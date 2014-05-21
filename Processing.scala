package processing

import scala.concurrent._

sealed trait Input[+I]

object Input {
  case class Element[+I](elem: I) extends Input[I]
  case object Empty extends Input[Nothing]
  case object End extends Input[Nothing]
}

/**
 * Represents the current state of a consumer
 */
sealed trait State[S, +I]

object State {
  case class Continue[S, I](process: Input[I] => Consumer[S, I]) extends State[S, I]
  case class Done[S, +I](finalState: S) extends State[S, I]
  case class Error[S](ex: Throwable) extends State[S, Nothing]

  /*
  def apply[I, S](state: S, input: Input[I] = Input.Empty): Consumer[S, I] = state match {
    case State.Done(state) => new StatefulConsumer[S, I] { def fold[B]() }
  }
  */
}

class Consumer[S, I](state: State[S, I])/*(fn: (S, I) => Consumer[S, I])*/ {

  def feed(input: I): Consumer[S, I] = state match {
    case State.Continue(process) => process(Input.Element(input))
    case State.Done(finalState)  => new Consumer(State.Done(finalState))
    case s: State.Error[S]       => new Consumer(s)
  }

  def feedAll(inputs: TraversableOnce[I]): Consumer[S, I] = inputs.foldLeft(this) { (acc, i) =>
    acc.feed(i)
  }

  def eof: Consumer[S, I] = state match {
    case State.Continue(process) => process(Input.End)
    case State.Done(finalState)  => new Consumer(State.Done(finalState))
    case s: State.Error[S]       => new Consumer(s)
  }

  def run: S = state match {
    case State.Done(finalState) => finalState
    case State.Error(ex)        => throw ex
    case _: State.Continue => sys.error("consumer not done")
  }

}

class AsyncConsumer[S, I](state: Future[State[S, I]])/*(fn: (S, I) => Consumer[S, I])*/ {

  def feed(input: Future[I]): AsyncConsumer[S, I] = new AsyncConsumer {
    state.collect {
      case State.Done(finalState)  => new AsyncConsumer(State.Done(finalState))
      case s: State.Error[S]       => new AsyncConsumer(s)
      case State.Continue(process) => process(Input.Element(input))
        val c = promise[State[S, I]]

        input.onComplete {
          case Success(i) => c.success(process(Input.Element(i)))
          case Failure(ex) => c.success(process(Input.Error(ex)))
        }

        c.future
    }
  }

  def feedAll(inputs: TraversableOnce[I]): Consumer[S, I] = inputs.foldLeft(this) { (acc, i) =>
    acc.feed(i)
  }

  def feedMAll(inputs: TraversableOnce[Future[I]])

  def feedAllM(inputs: Future[TraversableOnce[I]])

  def eof: Consumer[S, I] = state match {
    case State.Continue(process) => process(Input.End)
    case State.Done(finalState)  => new Consumer(State.Done(finalState))
    case s: State.Error[S]       => new Consumer(s)
  }

  def run: S = state match {
    case State.Done(finalState) => finalState
    case State.Error(ex)        => throw ex
    case _: State.Continue => sys.error("consumer not done")
  }

}

object Consumer {

  //def fold[S, I](fn: (S, I) => S): Consumer[S, I] = fold(State.Continue)

  /**
   * Creates a stateful consumer which folds over its input using
   * the given function and initial state
   */
  def fold[S, I](state: S)(fn: (S, I) => S): Consumer[S, I] = {

    def step(state: S)(input: Input[I]): Consumer[S, I] = input match {
      case Input.Element(elem) => new Consumer(State.Continue(step(fn(state, elem))))
      case Input.Empty         => new Consumer(State.Continue(step(state)))
      case Input.End           => new Consumer(State.Done(state))
    }

    new Consumer(State.Continue[S, I](step(state)))

  }

}

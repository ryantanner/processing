/*
sealed trait Input[+I]

object Input {
  case class Element[+I](elem: I) extends Input[I]
  case object Empty extends Input[Nothing]
  case object End extends Input[Nothing]
}
*/
/**
 * Represents the current state of a consumer
 */ /*
sealed trait State[S, +I]

object State {
  case class Continue[S, +I](process: Input[I] => Consumer[S, I]) extends State[S, I]
  case class Done[S, +I](finalState: S) extends State[S, I]
  case class Error[S](ex: Throwable) extends State[S, Nothing]

  def apply[I, S](state: S, input: Input[I] = Input.Empty): Consumer[S, I] = state match {
    case State.Done(state) => new StatefulConsumer[S, I] { def fold[B]() }
  }
}

trait Consumer[+S, I] {

  def fold[B](folder: State[S, I] => B): B

  def run: S = fold({
    case State.Done(finalState)   => finalState
    case State.Error(ex)          => throw ex
    case State.Continue(process)  => process(Input.End).fold({
      case State.Done(finalState) => finalState
      case State.Continue(_)      => throw new Exception("Consumer not done after input ended")
      case State.Error(ex)        => throw ex
    })
  })

}

object Consumer {
*/
  /**
   * Creates a stateful consumer which folds over its input using
   * the given function and initial state
   */ /*
  def fold[S, I](state: S)(fn: (S, I) => S): Consumer[S, I] = {

    def step(state: S)(input: Input[I]): Consumer[S, I] = input match {
      case Input.Element(elem) => State.Continue(step(fn(state, elem)))
      case Input.Empty         => State.Continue(step(state))
      case Input.End           => State.Done(state)
    }

    State.Continue[S, I](step(state))

  }

}

// A stateful consumer is its own state
trait StatefulConsumer[S, I] extends Consumer[S, I] with State[S, I] {

  def fold[B](folder: State[S, I] => B): B = {
    folder(this)
  }

}*/

package processors

sealed trait Input[+I]

object Input {
  case class Element[+I](elem: I) extends Input[I]
  case object Empty extends Input[Nothing]
  case object End extends Input[Nothing]
}

sealed trait State[S, +I]

object State {
  case class Continue[S, +I](process: Input[I] => Processor[S, I]) extends State[S, I]
  case class Done[S, +I](finalState: S) extends State[S, I]
  case object Error[I] extends State[Nothing, I]
}

trait Processor[+S, I] {

  // A purely side-effecting processor
  def process(input: Input[I]): Unit = input match {
    case Element(elem) => println(elem)
    case Empty         => println("Empty!")
    case End           => println("End!")
  }

}

// I could implement this as an actor
object Processor {

  // We could also have a stateful processsor
  // Note the recursion!
  def fold[S, I](state: S)(fn: (S, I) => S): Processor[S, I] =

    def step(state: S)(input: Input[I]): Processor[S, I] = input match {
      case Element(elem) => State.Continue(step(fn(state, elem)))
      case Empty         => State.Continue(step(state))
      case End           => State.Done(state)
    }

    State.Continue[S, I](step(state))

  }

}

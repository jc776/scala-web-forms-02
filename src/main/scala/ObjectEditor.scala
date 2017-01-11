package example

import org.scalajs.dom
import org.scalajs.dom.html
import scalatags.VDom.all._
import scalatags.events.AllEventsImplicits._

trait Editor[T] extends elm.Program[Frag] {
	// multiple kinds of Tree later. Doesn't fit "type class" well.
	def from(item: T): State
	def field(state: State): Option[T] // or, "Result"
}

trait Wrap[T] {
	val value: T
}

object Editor extends shapeless.LabelledProductTypeClassCompanion[Editor] {
	implicit val textEditor = new Editor[String] {
		type State = String
		type Action = String
		
		def view(send: Action => Unit, state: State) = input(
			`type` := "text",
			`class` := "form-control",
			value := state,
			oninput := {(e: dom.Event) => send(e.target.asInstanceOf[html.Input].value)}
		)
		def update(action: Action, state: State) = action
		val init = "" // implicitly[DefaultValue[String]] or ...
		def from(field: String) = field
		def field(state: State) = Some(state)
	}

	implicit val youMustTickEditor = new Editor[Boolean] {
		type State = Boolean
		type Action = Boolean

		def view(send: Action => Unit, state: State) = label(
			`class` := "form-check-label",
			input(`type` := "checkbox", `class` := "form-check-input"),
			"Only valid when checked.",
			checked := (if(state) "true" else "false"), // javascript!?
			onchange := {(e: dom.Event) => send(e.target.asInstanceOf[html.Input].checked)}
		)

		def update(action: Action, state: State) = action
		val init = false
		def from(field: Boolean) = field
		def field(state: State) = if(state) Some(true) else None
	}

	/*sealed trait ListAction
	implicit def listEditor[T](implicit editT: Editor[T]) = new Editor[List[T]] {
		type State = List[editT.State], Int
		type Action = 
	}*/

	/*
	tupleN = ProductTypeClass instead...

	implicit def pairEditor[T, U](implicit editT: Editor[T, Tag], editU: Editor[U, Tag]) = new Editor[(T,U), Tag] {
		type State = (editT.State, editU.State)
		type Action = Either[editT.Action, editU.Action]
		
		def view(send: Action => Unit, state: State) = ul(
			li("Left:",  editT.view(input => send(Left(input)),  state._1)),
			li("Right:", editU.view(input => send(Right(input)), state._2))
		)
		def update(action: Action, state: State) = action match {
			case Left(actT)  => (editT.update(actT, state._1), state._2)
			case Right(actU) => (state._1, editU.update(actU, state._2))
		}
		val init = (editT.init, editU.init)
		def from(field: (T,U)) = (editT.from(field._1), editU.from(field._2))
		def field(state: State) = {
			// if both valid then Some(t,u) else None
			for { t <- editT.field(state._1); u <- editU.field(state._2) } yield (t, u)
		}
	}
	*/
	
	object typeClass extends shapeless.LabelledProductTypeClass[Editor] {
		// 1. Do LabelledTypeClassCompanion and TypeClassCompanion conflict, or do I get a weird empty name?
		// 2. I'll need my own one for FieldNames[T] replacing keys, anyway.

		import shapeless._

		// Editor.of[{a: String, b: Int, c: Person}]
		def emptyProduct = new Editor[HNil] {
			type State = HNil
			type Action = Unit
			
			def view(send: Action => Unit, state: State) = Seq[Tag]()
			def update(action: Action, state: State) = state
			val init = HNil
			def from(field: HNil) = HNil
			def field(state: State) = Some(HNil)
		}
		def product[H, T <: HList](name: String, editH: Editor[H], editT: Editor[T]) = new Editor[H :: T] {
			type State = (editH.State, editT.State)
			type Action = Either[editH.Action, editT.Action]
			
			def view(send: Action => Unit, state: State) = Seq(
				div(
					`class` := "form-group row",
					label(`class` := "col-2 col-form-label", name),
					div(`class` := "col-10", editH.view(input => send(Left(input)),  state._1))
				),
				editT.view(input => send(Right(input)), state._2)
			)
			def update(action: Action, state: State) = action match {
				case Left(actH)  => (editH.update(actH, state._1), state._2)
				case Right(actT) => (state._1, editT.update(actT, state._2))
			}
			val init = (editH.init, editT.init)
			def from(field: H :: T) = (editH.from(field.head), editT.from(field.tail))
			def field(state: State) = {
				// if both valid then Some(t,u) else None
				for { head <- editH.field(state._1); tail <- editT.field(state._2) } yield ::(head,tail)
			}
		}
		/*
		// Editor.of[String | Int | Person]
		def emptyCoproduct = new Editor[CNil] {
			type State = Unit
			type Action = Unit
			
			def view(send: Action => Unit, state: State) = div()
			def update(action: Action, state: State) = state
			val init = ()
			def from(field: CNil) = Unit
			def field(state: State) = None // shouldn't be this one? dunno.
		}
		def coproduct[L, R <: Coproduct](name: String, editL: => Editor[L], editR: => Editor[R]) = new Editor[L :+: R] {
			//val editL = sl
			//val editR = sr
			type State = Either[editL.State, editR.State]
			// 4-way compare?
			// ignore unfitting ones? reset to init on compare?
			type Action = Unit
			
			def view(send: Action => Unit, state: State) = div("Not yet done.")
			def update(action: Action, state: State) = state
			
			val init = Left(editL.init)
			def from(field: L :+: R) = field match {
				case Inl(l) => Left(editL.from(l))
				case Inr(r) => Right(editR.from(r))
			}
			def field(state: State) = state match {
				case Left(l) => editL.field(l).map(Inl(_))
				case Right(r) => editR.field(r).map(Inr(_))
			}
		}
		*/
		class Project[F, G](val inst: Editor[G], to: F => G, fromG: G => F) extends Editor[F] {
			// {name: editString.State, text: editString.State} <-> Person(name: "a", text: "b")

			type State = inst.State
			type Action = inst.Action
			
			def view(send: Action => Unit, state: State) = inst.view(send, state)
			def update(action: Action, state: State) = inst.update(action, state)
			val init = inst.init
			def from(field: F): State = inst.from(to(field))
			def field(state: State): Option[F] = inst.field(state).map(fromG)
		}
		
		def project[F, G](instance: => Editor[G], to: F => G, fromG: G => F) = new Project(instance, to, fromG)
	}
	
	def of[T](implicit edit: Editor[T]) = edit
}
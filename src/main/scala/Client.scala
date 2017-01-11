package example

import scala.scalajs.js.annotation.JSExport

import org.scalajs.dom
import org.scalajs.dom.html
import scalatags.VDom.all._

@JSExport
object ScalaJSExample {

	val doc = dom.document.getElementById("app").asInstanceOf[html.Element]
	val log = dom.document.getElementById("log").asInstanceOf[html.Element]
	
	case class Person(name: String, text: String, address: Address, valid: Boolean)
	case class Address(line1: String, line2: String, line3: String, county: String, postcode: String)

	@JSExport
	def main(): Unit = {
		/* 1. it's an instance of trait, not a case-class
		   2. even if it was I might want to modify methods
		
		implicit val personEditor = implicitly[Editor[Person]].copy(
			init = Person("defaultName", "defaultText", Address("line1","line2","line3","county","AA0 0AA")
		)*/

		val api = adminPanel.memoryApi[Person]()
		val table = new adminPanel.TableEditor(api)

		val program = new elm.Program[Frag] {
			type State = table.State
			type Action = table.Action

			def view(send: Action => Unit, state: State) = Seq(
				h1("Web forms!"),
				div(`class` := "row")(
					div(`class` := "col-8", table.view(send, state)),
					div(`class` := "col-4")
				),
				pre(pprint.stringify(state))
			)
			def update(action: Action, state: State) = table.update(action, state)
			def init = table.init
		}

		val mailbox = elm.embed(program, doc)
		mailbox.send(adminPanel.ViewList)
	}
}

	// Pair of states
		//val editor = implicitly[Editor[Person]]
		/*val program = new elm.Program[Frag] {
			type State = (table.State, editor.State)
			type Action = Either[table.Action, editor.Action]

			def view(send: Action => Unit, state: State) = div(
				table.view((a: table.Action) => send(Left(a)), state._1),
				state._1.toString,
				editor.view((a: editor.Action) => send(Right(a)), state._2),
				state._2.toString
			)
			def update(action: Action, state: State) = action match {
				case Left(l)  => (table.update(l, state._1), state._2)
				case Right(r) => (state._1, editor.update(r, state._2))
			}
			def init = (table.init, editor.init) 
		}*/

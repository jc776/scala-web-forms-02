package example

import scala.scalajs.js
import org.scalajs.dom
import org.scalajs.dom.html
import scalatags.VDom.all._
import scalatags.vdom.raw.VirtualDom

@js.native
trait MorphDOM extends js.Object {
    def apply(tree: dom.Node, target: dom.Node/*, args: js.UndefOr...*/): dom.Node = js.native 
}

@js.native
object morphdom extends MorphDOM

object elm {
	trait Program[+Tree] {
		type State
		type Action
		
		def view(send: Action => Unit, state: State): Tree
		def update(action: Action, state: State): State
		def init: State 
		
		trait Mailbox {
			def state: State
			def send(action: Action): Unit
			def subscribe(callback: State => Unit): Unit
		}
	}
	// trait Renderer
	def embed[Tree <: Frag](program: Program[Tree], node: html.Element /*dom.Node?*/): program.Mailbox
		= new program.Mailbox {
			var state = program.init
            var currentTree = program.view(send _, state).render
			val child: dom.Node = VirtualDom.create(currentTree)
            node.appendChild(child)

			def render(): Unit = {
                val newTree = program.view(send _, state).render
                val df = VirtualDom.diff(currentTree, newTree)
                VirtualDom.patch(child, df)
                currentTree = newTree

				//node.innerHTML = ""
				//node.appendChild(program.view(send _, state).render)

                //val target = program.view(send _, state).render
                //child = morphdom(child, target)
			}
			

			
			def send(action: program.Action): Unit = {
				state = program.update(action, state)
				for(subscriber <- subscribers) { subscriber(state) }
				render()
			}
			
			var subscribers: List[program.State => Unit] = List()
			def subscribe(callback: program.State => Unit): Unit = { subscribers = callback :: subscribers }
		}
		
	//def mapAction[T, U](wrap: T => U)(send: U => Unit)(input: T): Unit = send(wrap(input))
	// Html.map SubAction (sub.view) <=> sub.view(x => send(SubAction(x)))
}
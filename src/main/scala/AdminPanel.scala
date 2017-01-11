package example

import org.scalajs.dom
import org.scalajs.dom.html
import scalatags.VDom.all._
import scalatags.events.AllEventsImplicits._

object adminPanel {
    case class Row[T](id: Long, item: T)

    trait Api[T] {
		def list(): List[Row[T]]
		def create(item: T): Long
		def edit(id: Long, item: T): Unit
		def delete(id: Long): Unit
	}

    def memoryApi[T]() = new Api[T] {
		var store = List[Row[T]]()
		
		def list() = store
		def create(item: T) = {
			val newId = store.map(_.id).foldLeft(0L)(_ max _) + 1L
			store = store :+ Row(newId, item)
			newId
		}
		def edit(id: Long, item: T) {
			store = store.map((r: Row[T]) => {
				if(r.id == id) 
					Row(r.id,item) 
				else 
					r
			})
		}
		def delete(id: Long) {
			store = store.filter(_.id != id)
		}
	}

    sealed trait TableUI[+EditT]
	case object ListUI extends TableUI[Nothing]
	case class EditUI[EditT](id: Long, state: EditT) extends TableUI[EditT]
	case class CreateUI[EditT](state: EditT) extends TableUI[EditT]

    case class TableState[T, EditT](
		data: Option[List[Row[T]]],
		ui: TableUI[EditT]
	)


    // Seq[+T] means Seq[Student] is a Seq[Person]
    //               None is an Option[T]

    sealed trait TableAction[+T, +EditAction]
    //case object NoOp extends TableAction[Nothing]
    case object ViewList extends TableAction[Nothing, Nothing]
    case class ViewEdit[T](id: Long, item: T) extends TableAction[T, Nothing]
    case object ViewCreate extends TableAction[Nothing, Nothing]
    case class EditAction[Action](action: Action) extends TableAction[Nothing, Action]
    // api-triggered actions don't have to be included...

	class TableEditor[T](val api: Api[T])(implicit val editT: Editor[T]) extends elm.Program[Frag] {
		type State = TableState[T, editT.State]
		type Action = TableAction[T, editT.Action]

        val btnDefault = `class` := "btn btn-default"
        val btnPrimary = `class` := "btn btn-primary"
        val btnDanger = `class` := "btn btn-danger"
        val gap = " "

		def update(action: Action, state: State) = action match {
            case ViewList => state.copy(data = Some(api.list()), ui = ListUI)
            case ViewEdit(id, item) => state.copy(ui = EditUI(id, editT.from(item)))
            case ViewCreate => state.copy(ui = CreateUI(editT.init))
            case EditAction(act) => state.copy(
                ui = state.ui match {
                    case EditUI(id, item) => EditUI(id, editT.update(act, item))
                    case CreateUI(item) => CreateUI(editT.update(act, item))
                    case x => x
                }
            )
        }

		def view(send: Action => Unit, state: State): Frag = {
            val cancelButton = button(btnDefault, "Cancel", onclick := {(e: dom.Event) => send(ViewList)})
			def viewRow(row: Row[T]) = li(
                s"#${row.id}, ${row.item}", button(btnDefault, "Edit", onclick := {(e: dom.Event) => send(ViewEdit(row.id, row.item))})
			)
			
			state.ui match {
				case ListUI => div(
                    h2("List"),
					state.data match {
						case Some(rows) => div(
                            button(btnDefault, "New", onclick := {(e: dom.Event) => send(ViewCreate)}),
							ul(rows.map(viewRow))
						)
						case None => span("Loading...")
					}
				)
				case EditUI(id, state) => {
                    val maybeItem = editT.field(state)
					div(
                        h2("Edit"),
                        editT.view((a: editT.Action) => send(EditAction(a)), state), 
						cancelButton,gap,
						maybeItem match {
                            case Some(item) => button(btnPrimary, "Save", onclick := {(e: dom.Event) => api.edit(id, item); send(ViewList)})
                            case None => "Invalid."
                        },gap,
                        button(btnDanger, "Delete", onclick := {(e: dom.Event) => api.delete(id); send(ViewList)})
					)
				}
				case CreateUI(state) => {
                    val item = editT.field(state)
                    div(
                        h2("Create"),
                        editT.view((a: editT.Action) => send(EditAction(a)), state), 
                        cancelButton,gap,
						editT.field(state) match {
                            case Some(item) => button(btnPrimary, "Create", onclick := {(e: dom.Event) => api.create(item); send(ViewList)})
                            case None => "Invalid."
                        }
				    )
                }
			}
		}
		val init = TableState[T, editT.State](None, ListUI)
	}
}
	


	

	
	

	

	
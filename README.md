![example](example.png)

- Editor[T]: Embeddable (elm-program) editor for any one type.
- TableEditor: search, view(+misc.), edit, create, delete. Talk to web APIs.
- Routing: Navbar, URLs, inter-links on tables.
- Elm-Program: `send` replaces `Cmd + Task + Sub + Html<Msg>`
- Virtual-DOM:
  - morphdom: Uses regular DOM, so scalatags.JsDom works
  - or, wrap it in `Lazy` (takes args) + `Tree` (draw it)
  - I still don't want to add `Html<Msg>`
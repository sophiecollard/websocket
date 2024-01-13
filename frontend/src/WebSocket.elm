port module WebSocket exposing (connect, onMessage)

import Json.Decode exposing (Value)


port connect : String -> Cmd msg


port onMessage : (Value -> msg) -> Sub msg

port module WebSocket exposing (close, connect, onMessage, sendMessage)

import Json.Decode exposing (Value)


port connect : String -> Cmd msg


port close : () -> Cmd msg


port sendMessage : Value -> Cmd msg


port onMessage : (Value -> msg) -> Sub msg

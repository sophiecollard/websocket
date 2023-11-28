port module WebSocket exposing (connect, onClose, onError, onMessage, onOpen)

import Json.Encode exposing (Value)


port connect : String -> Cmd msg


port onClose : (Int -> msg) -> Sub msg


port onError : (Value -> msg) -> Sub msg


port onMessage : (String -> msg) -> Sub msg


port onOpen : (Value -> msg) -> Sub msg

module Main exposing (..)

import Browser
import Html exposing (Html, br, button, div, h1, input, label, text, textarea)
import Html.Attributes exposing (class, placeholder, style, value)
import Html.Events exposing (onClick, onInput)
import Json.Decode
import Json.Encode
import WebSocket



-- MAIN


main =
    Browser.element
        { init = init
        , view = view
        , update = update
        , subscriptions = subscriptions
        }



-- MODEL


type Model
    = Welcome WelcomeData
    | Chat ChatData


type alias WelcomeData =
    { username : String
    }


type alias ChatData =
    { username : String
    , messages : List Message
    , currentMessage : String
    }


type alias Message =
    { contents : String
    , author : String
    }


messageDecoder : Json.Decode.Decoder Message
messageDecoder =
    Json.Decode.map2 Message
        (Json.Decode.field "contents" Json.Decode.string)
        (Json.Decode.field "author" Json.Decode.string)


init : () -> ( Model, Cmd Msg )
init _ =
    ( Welcome (WelcomeData ""), Cmd.none )



-- UPDATE


type Msg
    = UpdateUsername String
    | JoinChat
    | UpdateCurrentMessage String
    | PostCurrentMessage
    | HandleMessageEvent Json.Decode.Value
    | LeaveChat


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case ( msg, model ) of
        ( UpdateUsername username, Welcome welcomeData ) ->
            ( Welcome { welcomeData | username = username }
            , Cmd.none
            )

        ( JoinChat, Welcome welcomeData ) ->
            ( Chat (ChatData welcomeData.username [] "")
            , WebSocket.connect ("ws://localhost:8081/v1/chat?username=" ++ welcomeData.username)
            )

        ( UpdateCurrentMessage currentMessage, Chat chatData ) ->
            ( Chat { chatData | currentMessage = currentMessage }
            , Cmd.none
            )

        ( PostCurrentMessage, Chat chatData ) ->
            ( Chat { chatData | currentMessage = "" }
            , WebSocket.sendMessage (Json.Encode.string chatData.currentMessage)
            )

        ( HandleMessageEvent payload, Chat chatData ) ->
            case Json.Decode.decodeValue messageDecoder payload of
                Ok message ->
                    ( Chat (ChatData chatData.username (message :: chatData.messages) chatData.currentMessage)
                    , Cmd.none
                    )

                Err _ ->
                    ( model, Cmd.none )

        ( LeaveChat, Chat _ ) ->
            ( Welcome (WelcomeData "")
            , WebSocket.close ()
            )

        _ ->
            ( model, Cmd.none )



-- SUBSCRIPTIONS


subscriptions : Model -> Sub Msg
subscriptions _ =
    Sub.batch
        [ WebSocket.onMessage HandleMessageEvent
        ]



-- VIEW


view : Model -> Html Msg
view model =
    case model of
        Welcome welcomeData ->
            div [ class "container" ]
                [ br [] []
                , div [ class "content" ]
                    [ h1 [ class "title is-2" ]
                        [ text "Welcome" ]
                    , div [ class "field" ]
                        [ label [ class "label" ] [ text "Pick a username" ]
                        , input
                            [ class "input is-medium"
                            , placeholder "Choose a username"
                            , value welcomeData.username
                            , onInput UpdateUsername
                            ]
                            []
                        ]
                    , div [ class "field" ]
                        [ button
                            [ class "button is-info is-medium is-fullwidth"
                            , onClick JoinChat
                            ]
                            [ text "Join chat" ]
                        ]
                    ]
                ]

        Chat chatData ->
            div [ class "container" ]
                [ br [] []
                , div [ class "content" ]
                    [ h1 [ class "title is-2" ] [ text ("Welcome " ++ chatData.username) ]
                    ]

                -- Message history
                , div [ class "field" ]
                    [ textarea [ class "textarea", style "height" "70vh" ]
                        (chatData.messages |> List.reverse |> List.map (\m -> text (m.author ++ ": " ++ m.contents ++ "\n")))
                    ]

                -- New message field
                , div [ class "field" ]
                    [ input
                        [ class "input"
                        , value chatData.currentMessage
                        , onInput UpdateCurrentMessage
                        ]
                        []
                    ]

                -- Post message button
                , div [ class "field" ]
                    [ button
                        [ class "button is-info is-fullwidth"
                        , onClick PostCurrentMessage
                        ]
                        [ text "Post Message" ]
                    ]

                -- Leave button
                , div [ class "field" ]
                    [ button
                        [ class "button is-danger is-fullwidth"
                        , onClick LeaveChat
                        ]
                        [ text "Leave chat" ]
                    ]
                ]

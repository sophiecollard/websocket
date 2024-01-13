module Main exposing (..)

import Browser
import Html exposing (Html, br, button, div, h1, input, label, text, textarea)
import Html.Attributes exposing (class, placeholder, style, value)
import Html.Events exposing (onClick, onInput)
import Json.Decode
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
    { userId : String
    }


type alias ChatData =
    { userId : String
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
    = UpdateUserId String
    | JoinChat
    | HandleMessageEvent Json.Decode.Value


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case ( msg, model ) of
        ( UpdateUserId userId, Welcome welcomeData ) ->
            ( Welcome { welcomeData | userId = userId }
            , Cmd.none
            )

        ( JoinChat, Welcome welcomeData ) ->
            ( Chat (ChatData welcomeData.userId [] "")
            , WebSocket.connect ("ws://localhost:8081/v1/chat?userId=" ++ welcomeData.userId)
            )

        ( HandleMessageEvent payload, Chat chatData ) ->
            case Json.Decode.decodeValue messageDecoder payload of
                Ok message ->
                    ( Chat (ChatData chatData.userId (message :: chatData.messages) chatData.currentMessage)
                    , Cmd.none
                    )

                Err _ ->
                    ( model, Cmd.none )

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
                        [ label [ class "label" ] [ text "Pick a user ID" ]
                        , input
                            [ class "input is-medium"
                            , placeholder "ffe79323-3a82-4b16-9035-31c71d5cfbdf"
                            , value welcomeData.userId
                            , onInput UpdateUserId
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
                    [ h1 [ class "title is-2" ] [ text ("Welcome " ++ chatData.userId) ]
                    ]

                -- Message history
                , div [ class "field" ]
                    [ textarea [ class "textarea", style "height" "70vh" ]
                        [ text ("This is a message" ++ "\n" ++ "This is another") ]
                    ]

                -- New message field
                , div [ class "field" ]
                    [ input
                        [ class "input"
                        , value chatData.currentMessage
                        ]
                        []
                    ]

                -- Post message button
                , div [ class "field" ]
                    [ button
                        [ class "button is-info is-fullwidth" ]
                        [ text "Post Message" ]
                    ]
                ]

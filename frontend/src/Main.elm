module Main exposing (..)

import Browser
import Html exposing (Html, br, button, div, p, text)
import Html.Attributes exposing (class)
import Html.Events exposing (onClick)
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


type alias Model =
    List String


init : () -> ( Model, Cmd Msg )
init _ =
    ( [], Cmd.none )



-- UPDATE


type Msg
    = Connect
    | HandleOpenEvent Json.Decode.Value
    | HandleMessageEvent String
    | HandleErrorEvent Json.Decode.Value
    | HandleCloseEvent Int


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        Connect ->
            ( model, WebSocket.connect "ws://localhost:8081/v1/index/daily" )

        HandleOpenEvent _ ->
            ( model, Cmd.none )

        HandleMessageEvent message ->
            ( message :: model, Cmd.none )

        HandleErrorEvent _ ->
            ( model, Cmd.none )

        HandleCloseEvent _ ->
            ( model, Cmd.none )



-- SUBSCRIPTIONS


subscriptions : Model -> Sub Msg
subscriptions _ =
    Sub.batch
        [ WebSocket.onOpen HandleOpenEvent
        , WebSocket.onMessage HandleMessageEvent
        , WebSocket.onError HandleErrorEvent
        , WebSocket.onClose HandleCloseEvent
        ]



-- VIEW


view : Model -> Html Msg
view model =
    div [ class "container" ]
        [ br [] []
        , div [ class "content" ]
            (List.append
                [ button [ class "button is-info", onClick Connect ] [ text "Connect to WebSocket" ]
                ]
                (List.map (\v -> p [] [ text v ]) model)
            )
        ]

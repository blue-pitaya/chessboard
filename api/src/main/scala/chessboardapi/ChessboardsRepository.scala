package chessboardapi

import cats.effect.kernel.Ref
import cats.effect.IO
import chessboardcore.Vec2d
import chessboardcore.Model._
import chessboardcore.HttpModel

object ChessboardRepository {
  case class Entry(id: Long, boardSize: Vec2d, pieces: List[PlacedPiece])

  case class State(nextId: Long, entries: List[Entry])
  object State {
    def empty = State(nextId = 1, entries = List())

    def example = State(
      nextId = 2,
      entries = List(
        Entry(
          id = 1,
          boardSize = Vec2d(3, 3),
          pieces = List(
            PlacedPiece(
              pos = Vec2d(0, 0),
              piece = Piece(color = White, kind = Queen)
            ),
            PlacedPiece(
              pos = Vec2d(1, 1),
              piece = Piece(color = Black, kind = Queen)
            )
          )
        )
      )
    )
  }

  def createState(): IO[Ref[IO, State]] = Ref.of[IO, State](State.example)

  def append(
      stateRef: Ref[IO, State],
      data: HttpModel.CreateChessboard_In
  ): IO[Unit] = for {
    state <- stateRef.update(appendEntry(_, data))
  } yield ()

  def list(stateRef: Ref[IO, State]): IO[List[Entry]] = for {
    state <- stateRef.get
    entries = state.entries
  } yield (entries)

  private def appendEntry(
      state: State,
      data: HttpModel.CreateChessboard_In
  ): State = {
    val id = state.nextId
    val entry = Entry(id = id, boardSize = data.boardSize, pieces = data.pieces)

    state.copy(nextId = id + 1, entries = state.entries.appended(entry))
  }
}

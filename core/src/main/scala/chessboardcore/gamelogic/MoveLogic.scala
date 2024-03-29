package chessboardcore.gamelogic

import chessboardcore.Model._
import chessboardcore.Vec2d
import monocle.syntax.all._

// Standard chess moves, but no en passant or castling for now
// Pieces can't move if doing so will make king checked
// allow pawn only if rank is 2 (for white) or n-1 (for black) (in 1..n)

//TODO: can be sets all the way

object MoveLogic {

  private def pawnMoves(
      pos: Vec2d,
      color: PieceColor,
      boardHeight: Int,
      tileExists: Vec2d => Boolean,
      pieceOn: Vec2d => Option[Piece]
  ): List[Vec2d] = {
    val yDirection = color match {
      case Black => -1
      case White => 1
    }
    val isBaseRank = color match {
      case Black => pos.y == (boardHeight - 2)
      case White => pos.y == 1
    }

    val onUpMove = pos + Vec2d(0, yDirection)
    val twoUpMove = pos + Vec2d(0, 2 * yDirection)
    val attackMoves =
      List(pos + Vec2d(-1, yDirection), pos + Vec2d(1, yDirection))

    val possibleOnUpMove = Option.when(!pieceOn(onUpMove).isDefined)(onUpMove)
    val possibleTwoUpMove = Option.when(
      possibleOnUpMove.isDefined && !pieceOn(twoUpMove).isDefined && isBaseRank
    )(twoUpMove)
    val possibleAttackMoves = attackMoves.filter(p =>
      pieceOn(p) match {
        case Some(Piece(col, _)) if col != color => true
        case _                                   => false
      }
    )

    (List(possibleOnUpMove, possibleTwoUpMove).flatten ++ possibleAttackMoves)
      .filter(tileExists)
  }

  private def moveWhile(
      pos: Vec2d,
      step: Vec2d,
      cond: Vec2d => Boolean,
      takeLast: Vec2d => Boolean
  ): List[Vec2d] = {
    def f(acc: List[Vec2d], p: Vec2d): List[Vec2d] = {
      p match {
        case p if cond(p) => f(p :: acc, p + step)
        case p =>
          if (takeLast(p)) p :: acc
          else acc
      }
    }

    f(List(), pos + step)
  }

  private def rookMoves(
      pos: Vec2d,
      color: PieceColor,
      tileExists: Vec2d => Boolean,
      pieceOn: Vec2d => Option[Piece]
  ): List[Vec2d] = {
    val steps = List(Vec2d(1, 0), Vec2d(0, 1), Vec2d(-1, 0), Vec2d(0, -1))
    steps
      .map { step =>
        val cond = (v: Vec2d) => (tileExists(v) && pieceOn(v).isEmpty)
        val takeLast = (v: Vec2d) =>
          pieceOn(v) match {
            case Some(Piece(col, _)) if col != color => true
            case _                                   => false
          }

        moveWhile(pos, step, cond, takeLast)
      }
      .flatten
  }

  private def knightMoves(
      pos: Vec2d,
      color: PieceColor,
      tileExists: Vec2d => Boolean,
      pieceOn: Vec2d => Option[Piece]
  ): List[Vec2d] = {
    val steps = List(
      Vec2d(-2, -1),
      Vec2d(-2, 1),
      Vec2d(-1, 2),
      Vec2d(1, 2),
      Vec2d(2, 1),
      Vec2d(2, -1),
      Vec2d(1, -2),
      Vec2d(-1, -2)
    )

    steps
      .map { step =>
        val move = pos + step
        Option.when(
          tileExists(move) &&
            (pieceOn(move) match {
              case Some(Piece(col, _)) if col != color => true
              case None                                => true
              case _                                   => false
            })
        )(move)
      }
      .flatten
  }

  private def bishopMoves(
      pos: Vec2d,
      color: PieceColor,
      tileExists: Vec2d => Boolean,
      pieceOn: Vec2d => Option[Piece]
  ): List[Vec2d] = {
    val steps = List(Vec2d(1, 1), Vec2d(-1, 1), Vec2d(-1, -1), Vec2d(1, -1))
    steps
      .map { step =>
        val cond = (v: Vec2d) => (tileExists(v) && pieceOn(v).isEmpty)
        val takeLast = (v: Vec2d) =>
          pieceOn(v) match {
            case Some(Piece(col, _)) if col != color => true
            case _                                   => false
          }

        moveWhile(pos, step, cond, takeLast)
      }
      .flatten
  }

  private def queenMoves(
      bishopMoves: List[Vec2d],
      rookMoves: List[Vec2d]
  ): List[Vec2d] = bishopMoves ++ rookMoves

  private def kingMoves(
      pos: Vec2d,
      color: PieceColor,
      tileExists: Vec2d => Boolean,
      pieceOn: Vec2d => Option[Piece]
  ): List[Vec2d] = {
    val steps = List(
      Vec2d(1, 0),
      Vec2d(1, 1),
      Vec2d(0, 1),
      Vec2d(-1, 1),
      Vec2d(-1, 0),
      Vec2d(-1, -1),
      Vec2d(0, -1),
      Vec2d(1, -1)
    )

    steps
      .map { step =>
        val move = pos + step
        Option.when(
          tileExists(move) &&
            (pieceOn(move) match {
              case Some(Piece(col, _)) if col != color => true
              case None                                => true
              case _                                   => false
            })
        )(move)
      }
      .flatten
  }

  // FIXME
  private def simulateMove(board: Board, from: Vec2d, to: Vec2d): Board = {
    lazy val lens = board.focus(_.pieces)

    board.pieces.get(from) match {
      case None        => board
      case Some(piece) => lens.modify(_.removed(from).updated(to, piece))
    }
  }

  def possibleMovesIgnoringPossibleCheck(
      board: Board,
      from: Vec2d
  ): List[Vec2d] = {
    val pieces = board.pieces
    val pieceOn = (v: Vec2d) => pieces.get(v)
    val tileExists = (v: Vec2d) =>
      (v.x >= 0 && v.y >= 0 && v.x < board.size.x && v.y < board.size.y)
    val pieceOpt = pieceOn(from)

    pieceOpt match {
      case None => List()
      case Some(Piece(color, kind)) =>
        lazy val _bishopMoves = bishopMoves(from, color, tileExists, pieceOn)
        lazy val _rookMoves = rookMoves(from, color, tileExists, pieceOn)
        kind match {
          case King   => kingMoves(from, color, tileExists, pieceOn)
          case Rook   => _rookMoves
          case Knight => knightMoves(from, color, tileExists, pieceOn)
          case Bishop => _bishopMoves
          case Queen  => queenMoves(_bishopMoves, _rookMoves)
          case Pawn => pawnMoves(from, color, board.size.y, tileExists, pieceOn)
        }
    }
  }

  def possibleMoves(board: Board, from: Vec2d): List[Vec2d] = board
    .pieces
    .get(from)
    .map { piece =>
      possibleMovesIgnoringPossibleCheck(board, from).filter { to =>
        val simulatedMoveBoard = simulateMove(board, from, to)
        !isKingChecked(simulatedMoveBoard, piece.color)
      }
    }
    .getOrElse(List())

  def canMove(board: Board, from: Vec2d, to: Vec2d): Boolean =
    possibleMoves(board, from).contains(to)

  def isKingChecked(board: Board, color: PieceColor): Boolean =
    kingPos(board, color) match {
      case None => false
      case Some(pos) => allPiecesOfColor(board, PieceColor.opposite(color))
          .flatMap { case (pos, _) =>
            possibleMovesIgnoringPossibleCheck(board, pos)
          }
          .contains(pos)
    }

  def kingPos(board: Board, color: PieceColor): Option[Vec2d] = board
    .pieces
    .find { case (pos, piece) =>
      piece == Piece(color, King)
    }
    .map(_._1)

  def allPiecesOfColor(board: Board, color: PieceColor): List[(Vec2d, Piece)] =
    board
      .pieces
      .toList
      .filter { case (_, piece) =>
        piece.color == color
      }
}

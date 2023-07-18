package chessboardcore.gamelogic

import chessboardcore.Vec2d
import chessboardcore.Model._

// Standard chess moves, but no en passant or castling for now
// Pieces can't move if doing so will make king checked
// allow pawn only if rank is 2 (for white) or n-1 (for black) (in 1..n)

object MoveLogic {

  def pawnMoves(
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

  def rookMoves(
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

  def knightMoves(
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

  def bishopMoves(
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

  def queenMoves(
      bishopMoves: List[Vec2d],
      rookMoves: List[Vec2d]
  ): List[Vec2d] = bishopMoves ++ rookMoves

  def kingMoves(
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

  def canMove(board: Board, from: Vec2d, to: Vec2d): Boolean = {
    val pieces = board.pieces.map(p => (p.pos, p.piece)).toMap
    val pieceOn = (v: Vec2d) => pieces.get(v)
    val tileExists = (v: Vec2d) =>
      (v.x >= 0 && v.y >= 0 && v.x < board.size.x && v.y < board.size.y)

    pieceOn(from) match {
      case None => false
      case Some(Piece(color, kind)) =>
        lazy val _bishopMoves = bishopMoves(from, color, tileExists, pieceOn)
        lazy val _rookMoves = rookMoves(from, color, tileExists, pieceOn)
        val possibleMoves = kind match {
          case King   => kingMoves(from, color, tileExists, pieceOn)
          case Rook   => _rookMoves
          case Knight => knightMoves(from, color, tileExists, pieceOn)
          case Bishop => _bishopMoves
          case Queen  => queenMoves(_bishopMoves, _rookMoves)
          case Pawn => pawnMoves(from, color, board.size.y, tileExists, pieceOn)
        }

        possibleMoves.contains(to)
    }
  }
}

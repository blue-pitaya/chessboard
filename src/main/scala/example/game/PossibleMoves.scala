package example.game

import example.GameState
import example.Move
import example.Utils.takeWhileInclusive
import example.models._
import xyz.bluepitaya.common.Vec2d
import example.GameMove
import example.CastlingMove

object PossibleMoves {

  private def lineAttacks(
      pos: Vec2d,
      nextPos: Vec2d => Vec2d,
      isPieceOn: Vec2d => Boolean,
      isInsideBoard: Vec2d => Boolean
  ) = {
    val start = nextPos(pos)
    val stop = (v: Vec2d) => isPieceOn(v)
    val stopBefore = (v: Vec2d) => !isInsideBoard(v)

    takeWhileInclusive(start, nextPos, stop, stopBefore).toSet
  }

  private def jumpAttacks(
      pos: Vec2d,
      nextPos: Vec2d => Vec2d,
      isInsideBoard: Vec2d => Boolean
  ): Set[Vec2d] = {
    val move = nextPos(pos)

    if (isInsideBoard(move)) Set(move)
    else Set()
  }

  private def lineCountedNonaggressiveMove(
      pos: Vec2d,
      maxSteps: Int,
      nextPos: Vec2d => Vec2d,
      isInsideBoard: Vec2d => Boolean,
      isPieceOn: Vec2d => Boolean
  ) = {
    case class PosStep(v: Vec2d, n: Int)
    val stop = (ps: PosStep) => ps.n == maxSteps
    val stopBefore = (ps: PosStep) => !isInsideBoard(ps.v) || isPieceOn(ps.v)
    val start = PosStep(nextPos(pos), 1)
    val next = (ps: PosStep) => PosStep(nextPos(ps.v), ps.n + 1)

    takeWhileInclusive(start, next, stop, stopBefore).map(_.v).toSet
  }

  private def getPieceAttacks(
      pos: Vec2d,
      piece: Piece,
      jumpAttackSteps: Vec2d => Set[Vec2d],
      lineAttackSteps: Vec2d => Set[Vec2d]
  ) = {
    def mirroredVecs(vecs: Set[Vec2d]) = vecs ++ vecs.map(_ * -1)
    def attacks(steps: Set[Vec2d], extendSteps: Vec2d => Set[Vec2d]) = steps
      .flatMap(extendSteps)

    val kingSteps =
      mirroredVecs(Set(Vec2d(0, 1), Vec2d(1, 1), Vec2d(1, 0), Vec2d(1, -1)))
    val bishopSteps = mirroredVecs(Set(Vec2d(1, 1), Vec2d(-1, 1)))
    val rookSteps = mirroredVecs(Set(Vec2d(0, 1), Vec2d(1, 0)))
    val queenSteps = rookSteps ++ bishopSteps
    val knightSteps =
      mirroredVecs(Set(Vec2d(2, 1), Vec2d(1, 2), Vec2d(-2, 1), Vec2d(-1, 2)))
    val whitePawnSteps = Set(Vec2d(-1, 1), Vec2d(1, 1))
    val blackPawnSteps = Set(Vec2d(-1, -1), Vec2d(1, -1))

    piece match {
      case Piece(King, _)     => attacks(kingSteps, jumpAttackSteps)
      case Piece(Bishop, _)   => attacks(bishopSteps, lineAttackSteps)
      case Piece(Knight, _)   => attacks(knightSteps, jumpAttackSteps)
      case Piece(Queen, _)    => attacks(queenSteps, lineAttackSteps)
      case Piece(Rook, _)     => attacks(rookSteps, lineAttackSteps)
      case Piece(Pawn, White) => attacks(whitePawnSteps, jumpAttackSteps)
      case Piece(Pawn, Black) => attacks(blackPawnSteps, jumpAttackSteps)
    }
  }

  private def getAllAttacks(
      pieces: Map[Vec2d, Piece],
      color: PieceColor,
      attacks: (Vec2d, Piece) => Set[Vec2d]
  ): Set[Vec2d] = pieces
    .filter { case (_, piece) =>
      piece.color == color
    }
    .flatMap { case (pos, piece) =>
      attacks(pos, piece)
    }
    .toSet

  private def isKingUnderCheck(
      pieces: Map[Vec2d, Piece],
      kingColor: PieceColor,
      enemyAttacks: Set[Vec2d]
  ): Boolean = pieces
    .find { case (_, piece) =>
      piece.kind == King && piece.color == kingColor
    }
    .map { case (pos, _) =>
      enemyAttacks.contains(pos)
    }
    .getOrElse(false) // king can't be checked if there is no king ¯\_(ツ)_/¯

  private def movesFromAttacks(
      attacks: Set[Vec2d],
      isSameColor: Vec2d => Boolean
  ) = attacks.filter(isSameColor)

  private def maxPawnSteps(pos: Vec2d, color: PieceColor) = color match {
    case Black if pos.y == 6 => 2
    case White if pos.y == 1 => 2
    case _                   => 1
  }

  def getEnPassantMoves(
      pos: Vec2d,
      color: PieceColor,
      lastMove: Option[GameMove]
  ): Set[Vec2d] = lastMove
    .map {
      case Move(piece, from, to) =>
        val lastMoveIsNearPawnDoubleUp = piece.kind == Pawn &&
          Math.abs(from.y - to.y) == 2 && Math.abs(pos.x - to.x) == 1

        val lastMovedPieceIsOnSameRank = to.y == pos.y

        lazy val attackStep = color match {
          case Black => Vec2d(to.x - pos.x, -1)
          case White => Vec2d(to.x - pos.x, 1)
        }

        if (lastMoveIsNearPawnDoubleUp && lastMovedPieceIsOnSameRank)
          Seq(pos + attackStep)
        else Seq()
      case _ => Seq()
    }
    .getOrElse(Seq())
    .toSet

  private val nextPos = (step: Vec2d) => (v: Vec2d) => v + step

  private val isInsideBoard = (size: Vec2d) =>
    (v: Vec2d) => (v.x >= 0 && v.y >= 0 && v.x < size.x && v.y < size.y)

  private val isPieceOn =
    (pieces: Map[Vec2d, Piece]) => (v: Vec2d) => pieces.isDefinedAt(v)

  private def getAttacks(pos: Vec2d, piece: Piece, state: GameState) = {
    val _isInsideBoard = isInsideBoard(state.size)
    val _isPieceOn = isPieceOn(state.pieces)
    val jumpAttackSteps =
      (step: Vec2d) => jumpAttacks(pos, nextPos(step), _isInsideBoard)
    val lineAttackSteps = (step: Vec2d) =>
      lineAttacks(pos, nextPos(step), _isPieceOn, _isInsideBoard)

    getPieceAttacks(pos, piece, jumpAttackSteps, lineAttackSteps)
  }

  private def _getMoves(
      pos: Vec2d,
      piece: Piece,
      state: GameState
  ): Set[GameMove] = {
    val currentPieceAttacks = getAttacks(pos, piece, state)
    val toMove = (toPos: Vec2d) => Move(piece, pos, toPos)

    val color = piece.color
    val isOppositeColor =
      (v: Vec2d) => state.pieces.get(v).map(_.color != color)
    val isOppositeColorOrEmpty =
      (v: Vec2d) => isOppositeColor(v).getOrElse(true)
    val standardMoves: Set[GameMove] = currentPieceAttacks
      .filter(isOppositeColorOrEmpty)
      .map(toMove)

    val moves: Set[GameMove] = piece.kind match {
      case Pawn =>
        val isOppositeColorOrNonEmpty =
          (v: Vec2d) => isOppositeColor(v).getOrElse(false)
        val attackMoves = currentPieceAttacks.filter(isOppositeColorOrNonEmpty)
        val pawnStep = (color: PieceColor) =>
          color match {
            case Black => Vec2d(0, -1)
            case White => Vec2d(0, 1)
          }

        val normalMoves = lineCountedNonaggressiveMove(
          pos,
          maxPawnSteps(pos, color),
          nextPos(pawnStep(color)),
          isInsideBoard(state.size),
          isPieceOn(state.pieces)
        )
        val enPassantMoves = getEnPassantMoves(pos, color, state.lastMove)

        (attackMoves ++ normalMoves ++ enPassantMoves).map(toMove)

      case King =>
        val rooks = state
          .pieces
          .filter { case (pos, piece) =>
            piece.kind == Rook
          }
        val attacks = (v: Vec2d, p: Piece) => getAttacks(v, p, state)
        val allAttacks = getAllAttacks(state.pieces, color.opposite, attacks)
        val isAttackOn = (pos: Vec2d) => allAttacks.contains(pos)
        val castleMove = (kingPos: Vec2d, rookPos: Vec2d) =>
          Castling.getCastleKingMove(
            color,
            kingPos,
            rookPos,
            state.hasPieceMoved,
            isPieceOn(state.pieces),
            isAttackOn
          )
        val castlingMoves = Castling.getMoves(color, pos, rooks, castleMove)

        standardMoves ++ castlingMoves

      case _ => standardMoves
    }

    moves.filter { move =>
      val simulatedState = GameLogic.forceMove(move, state)
      val attacks = (v: Vec2d, p: Piece) => getAttacks(v, p, simulatedState)
      val enemyAttacks =
        getAllAttacks(simulatedState.pieces, color.opposite, attacks)

      !isKingUnderCheck(simulatedState.pieces, color, enemyAttacks)
    }
  }

  def getMoves(pos: Vec2d, state: GameState) = state
    .pieces
    .get(pos)
    .map(_getMoves(pos, _, state))
    .getOrElse(Set())

  def getMoveTiles(pos: Vec2d, state: GameState): Map[Vec2d, GameMove] =
    getMoves(pos: Vec2d, state: GameState)
      .map {
        case m @ CastlingMove(kingMove, rookMove) => kingMove.to -> m
        case m @ Move(piece, from, to)            => to -> m
      }
      .toMap
}

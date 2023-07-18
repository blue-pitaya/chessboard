package example.game

//import example.game._
//import example.models._
//import org.scalatest.flatspec.AnyFlatSpec
//import org.scalatest.matchers.should.Matchers
//import chessboardcore.Vec2d
//
//class PossibleMovesSpec extends AnyFlatSpec with Matchers {
//  val boardSize: Vec2d = Vec2d(8, 8)
//
//  "knight moves" should "be in shape of L" in {
//    val pos = Vec2d(4, 3)
//    val piece = Piece(Knight, White)
//    val state = GameState(
//      size = boardSize,
//      pieces = Map(
//        pos -> piece,
//        Vec2d(2, 2) -> Piece(Pawn, White),
//        Vec2d(2, 4) -> Piece(Pawn, Black)
//      )
//    )
//    val expected = Set(
//      Vec2d(2, 4),
//      Vec2d(3, 5),
//      Vec2d(3, 1),
//      Vec2d(5, 5),
//      Vec2d(5, 1),
//      Vec2d(6, 2),
//      Vec2d(6, 4)
//    )
//
//    PossibleMoves.getMoveTiles(Vec2d(4, 3), state).map(_._1).toSet shouldEqual
//      expected
//  }
//
//  "bishop moves" should "be diagonal and blocked by pieces" in {
//    val piece = Piece(Bishop, White)
//    val state = GameState(
//      size = Vec2d(5, 4),
//      pieces = Map(
//        Vec2d(2, 1) -> piece,
//        Vec2d(0, 3) -> Piece(Pawn, White),
//        Vec2d(1, 0) -> Piece(Pawn, Black)
//      )
//    )
//    val expected =
//      Set(Vec2d(1, 2), Vec2d(1, 0), Vec2d(3, 0), Vec2d(3, 2), Vec2d(4, 3))
//
//    PossibleMoves.getMoveTiles(Vec2d(2, 1), state).map(_._1).toSet shouldEqual
//      expected
//  }
//
//  "bishop" should "not be able to move outside board" in {
//    val pos = Vec2d(0, 0)
//    val piece = Piece(Bishop, White)
//    val state = GameState(size = Vec2d(2, 2), pieces = Map(pos -> piece))
//    val expected = Set(Vec2d(1, 1))
//
//    PossibleMoves.getMoveTiles(pos, state).map(_._1).toSet shouldEqual expected
//  }
//
//  "rook moves" should "be straight and blocked by pieces" in {
//    val piece = Piece(Rook, White)
//    val state = GameState(
//      size = Vec2d(4, 4),
//      pieces = Map(
//        Vec2d(2, 1) -> piece,
//        Vec2d(0, 1) -> Piece(Pawn, White),
//        Vec2d(2, 3) -> Piece(Pawn, Black)
//      )
//    )
//    val expected =
//      Set(Vec2d(1, 1), Vec2d(2, 0), Vec2d(3, 1), Vec2d(2, 2), Vec2d(2, 3))
//
//    PossibleMoves.getMoveTiles(Vec2d(2, 1), state).map(_._1).toSet shouldEqual
//      expected
//  }
//
//  "queen moves" should "be straight, diagonal and blocked by pieces" in {
//    val piece = Piece(Queen, White)
//    val state = GameState(
//      size = Vec2d(4, 4),
//      pieces = Map(
//        Vec2d(2, 1) -> piece,
//        Vec2d(0, 1) -> Piece(Pawn, White),
//        Vec2d(2, 3) -> Piece(Pawn, Black)
//      )
//    )
//    val expected = Set(
//      Vec2d(1, 0),
//      Vec2d(2, 0),
//      Vec2d(3, 0),
//      Vec2d(1, 1),
//      Vec2d(3, 1),
//      Vec2d(1, 2),
//      Vec2d(2, 2),
//      Vec2d(3, 2),
//      Vec2d(0, 3),
//      Vec2d(2, 3)
//    )
//
//    PossibleMoves.getMoveTiles(Vec2d(2, 1), state).map(_._1).toSet shouldEqual
//      expected
//  }
//
//  // starting file in real board (files 1..8) is 2 for white and 7 for black
//  "pawn" should "be able to move 2 tiles on starting file" in {
//    val pos = Vec2d(0, 1)
//    val piece = Piece(Pawn, White)
//    val state = GameState(size = Vec2d(1, 4), pieces = Map(pos -> piece))
//    val expected = Set(Vec2d(0, 2), Vec2d(0, 3))
//
//    PossibleMoves.getMoveTiles(pos, state).map(_._1).toSet shouldEqual expected
//  }
//
//  "pawn" should
//    "not be able to move 2 tiles on starting file if path is blocked" in {
//      val pos = Vec2d(0, 1)
//      val piece = Piece(Pawn, White)
//      val state = GameState(
//        size = Vec2d(1, 4),
//        pieces = Map(pos -> piece, Vec2d(0, 2) -> Piece(Pawn, Black))
//      )
//      val expected = Set()
//
//      PossibleMoves.getMoveTiles(pos, state).map(_._1).toSet shouldEqual
//        expected
//    }
//
//  "pawn regular moves" should "be 1 to front" in {
//    val pos = Vec2d(0, 5)
//    val piece = Piece(Pawn, Black)
//    val state = GameState(size = Vec2d(1, 8), pieces = Map(pos -> piece))
//    val expected = Set(Vec2d(0, 4))
//
//    PossibleMoves.getMoveTiles(pos, state).map(_._1).toSet shouldEqual expected
//  }
//
//  "pawn regular moves" should "include possible attacks" in {
//    val pos = Vec2d(0, 2)
//    val piece = Piece(Pawn, White)
//    val state = GameState(
//      size = Vec2d(2, 8),
//      pieces = Map(pos -> piece, Vec2d(1, 3) -> Piece(Pawn, Black))
//    )
//    val expected = Set(Vec2d(0, 3), Vec2d(1, 3))
//
//    PossibleMoves.getMoveTiles(pos, state).map(_._1).toSet shouldEqual expected
//  }
//
//  "white pawn" should "be able to do en passant" in {
//    val pos = Vec2d(1, 4)
//    val piece = Piece(Pawn, White)
//    val state = GameState(
//      size = Vec2d(8, 8),
//      pieces = Map(pos -> piece, Vec2d(2, 4) -> Piece(Pawn, Black))
//    ).addMoveToHistory(Move(Piece(Pawn, Black), Vec2d(2, 6), Vec2d(2, 4)))
//    val expected = Set(Vec2d(1, 5), Vec2d(2, 5))
//
//    PossibleMoves.getMoveTiles(pos, state).map(_._1).toSet shouldEqual expected
//  }
//
//  "black pawn" should "be able to do en passant" in {
//    val pos = Vec2d(1, 3)
//    val piece = Piece(Pawn, Black)
//    val state = GameState(
//      size = Vec2d(8, 8),
//      pieces = Map(pos -> piece, Vec2d(0, 3) -> Piece(Pawn, White))
//    ).addMoveToHistory(Move(Piece(Pawn, White), Vec2d(0, 1), Vec2d(0, 3)))
//    val expected = Set(Vec2d(0, 2), Vec2d(1, 2))
//
//    PossibleMoves.getMoveTiles(pos, state).map(_._1).toSet shouldEqual expected
//  }
//
//  "pawn" should
//    "not be able to do en passant if is on diffrent rank than pawn which moves previously" in {
//      val pos = Vec2d(1, 5)
//      val piece = Piece(Pawn, White)
//      val state = GameState(
//        size = Vec2d(8, 8),
//        pieces = Map(pos -> piece, Vec2d(2, 4) -> Piece(Pawn, Black))
//      ).addMoveToHistory(Move(Piece(Pawn, Black), Vec2d(2, 6), Vec2d(2, 4)))
//      val expected = Set(Vec2d(1, 6))
//
//      PossibleMoves.getMoveTiles(pos, state).map(_._1).toSet shouldEqual
//        expected
//    }
//
//  "pawn" should "not be able to go outside board" in {
//    val pos = Vec2d(0, 7)
//    val piece = Piece(Pawn, White)
//    val state = GameState(size = Vec2d(1, 8), pieces = Map(pos -> piece))
//    val expected = Set()
//
//    PossibleMoves.getMoveTiles(pos, state).map(_._1).toSet shouldEqual expected
//  }
//
//  "king" should "not be able to walk to other king" in {
//    val pos = Vec2d(2, 1)
//    val piece = Piece(King, White)
//    val state = GameState(
//      size = Vec2d(3, 4),
//      pieces = Map(
//        Vec2d(0, 1) -> Piece(King, Black),
//        pos -> piece,
//        Vec2d(1, 3) -> Piece(Rook, White),
//        Vec2d(2, 0) -> Piece(Pawn, White)
//      )
//    )
//    val expected = Set(Vec2d(2, 2))
//
//    PossibleMoves.getMoveTiles(pos, state).map(_._1).toSet shouldEqual expected
//  }
//
//  "king" should "not be able to walk to position which is under attack" in {
//    val pos = Vec2d(0, 0)
//    val piece = Piece(King, White)
//    val state = GameState(
//      size = Vec2d(3, 3),
//      pieces = Map(
//        pos -> piece,
//        Vec2d(0, 2) -> Piece(Knight, Black),
//        Vec2d(2, 0) -> Piece(Bishop, Black)
//      )
//    )
//    val expected = Set(Vec2d(0, 1))
//
//    PossibleMoves.getMoveTiles(pos, state).map(_._1).toSet shouldEqual expected
//  }
//
//  "king" should
//    "not be able to take piece which is in position that is under attack by another piece" in {
//      val pos = Vec2d(0, 0)
//      val piece = Piece(King, White)
//      val state = GameState(
//        size = Vec2d(2, 2),
//        pieces = Map(
//          pos -> piece,
//          Vec2d(0, 1) -> Piece(Pawn, Black),
//          Vec2d(1, 1) -> Piece(Rook, Black)
//        )
//      )
//      val expected = Set(Vec2d(1, 1))
//
//      PossibleMoves.getMoveTiles(pos, state).map(_._1).toSet shouldEqual
//        expected
//    }
//
//  "king" should "move from check" in {
//    val pos = Vec2d(1, 1)
//    val piece = Piece(King, White)
//    val state = GameState(
//      size = Vec2d(3, 3),
//      pieces = Map(pos -> piece, Vec2d(0, 1) -> Piece(Queen, Black))
//    )
//    val expected = Set(Vec2d(2, 0), Vec2d(2, 2), Vec2d(0, 1))
//
//    PossibleMoves.getMoveTiles(pos, state).map(_._1).toSet shouldEqual expected
//  }
//
//  "piece" should
//    "not be able to move if king is under check and move dont prevent check" in {
//      val pos = Vec2d(0, 0)
//      val piece = Piece(Rook, White)
//      val state = GameState(
//        size = Vec2d(3, 3),
//        pieces = Map(
//          pos -> piece,
//          Vec2d(2, 0) -> Piece(King, White),
//          Vec2d(1, 1) -> Piece(Pawn, Black)
//        )
//      )
//      val expected = Set()
//
//      PossibleMoves.getMoveTiles(pos, state).map(_._1).toSet shouldEqual
//        expected
//    }
//
//  "piece" should
//    "be able to move when king is under check if it prevents check" in {
//      val pos = Vec2d(0, 1)
//      val piece = Piece(Rook, White)
//      val state = GameState(
//        size = Vec2d(3, 3),
//        pieces = Map(
//          pos -> piece,
//          Vec2d(2, 0) -> Piece(King, White),
//          Vec2d(2, 2) -> Piece(Queen, Black)
//        )
//      )
//      val expected = Set(Vec2d(2, 1))
//
//      PossibleMoves.getMoveTiles(pos, state).map(_._1).toSet shouldEqual
//        expected
//    }
//
//  "pawn" should "be able to stop check by doing en passant" in {
//    val pos = Vec2d(1, 3)
//    val piece = Piece(Pawn, Black)
//    val state = GameState(
//      size = Vec2d(8, 8),
//      pieces = Map(
//        pos -> piece,
//        Vec2d(0, 3) -> Piece(Pawn, White),
//        Vec2d(1, 4) -> Piece(King, Black)
//      )
//    ).addMoveToHistory(Move(Piece(Pawn, White), Vec2d(0, 1), Vec2d(0, 3)))
//    val expected = Set(Vec2d(0, 2))
//
//    PossibleMoves.getMoveTiles(pos, state).map(_._1).toSet shouldEqual expected
//
//  }
//
//  "piece" should
//    "not be able to move is performing move puts king under check" in {
//      val pos = Vec2d(2, 1)
//      val piece = Piece(Bishop, White)
//      val state = GameState(
//        size = Vec2d(3, 3),
//        pieces = Map(
//          pos -> piece,
//          Vec2d(2, 0) -> Piece(King, White),
//          Vec2d(2, 2) -> Piece(Queen, Black)
//        )
//      )
//      val expected = Set()
//
//      PossibleMoves.getMoveTiles(pos, state).map(_._1).toSet shouldEqual
//        expected
//    }
//
//  "king" should
//    "not be able to walk to position which is under attack by pawn" in {
//      val pos = Vec2d(1, 1)
//      val piece = Piece(King, White)
//      val state = GameState(
//        size = Vec2d(3, 3),
//        pieces = Map(
//          pos -> piece,
//          Vec2d(0, 1) -> Piece(Pawn, Black),
//          Vec2d(1, 2) -> Piece(Pawn, Black),
//          Vec2d(2, 1) -> Piece(Pawn, Black)
//        )
//      )
//      val expected =
//        Set(Vec2d(0, 0), Vec2d(2, 0), Vec2d(0, 2), Vec2d(1, 2), Vec2d(2, 2))
//
//      PossibleMoves.getMoveTiles(pos, state).map(_._1).toSet shouldEqual
//        expected
//    }
//
//  private val whiteKingStartPos = Vec2d(4, 0)
//
//  "king" should "not be able to castle when he has already moved" in {
//    val pos = whiteKingStartPos
//    val piece = Piece(King, White)
//    val state = GameState(
//      size = Vec2d(8, 8),
//      pieces = Map(pos -> piece, Vec2d(0, 0) -> Piece(Rook, White)),
//      moveHistory = Vector(
//        Move(piece, whiteKingStartPos, Vec2d(3, 0)),
//        Move(piece, Vec2d(3, 0), whiteKingStartPos)
//      )
//    )
//    val normalMoves =
//      Set(Vec2d(3, 0), Vec2d(5, 0), Vec2d(3, 1), Vec2d(4, 1), Vec2d(5, 1))
//    val expected = normalMoves
//
//    PossibleMoves.getMoveTiles(pos, state).map(_._1).toSet shouldEqual expected
//  }
//
//  "king" should "not be able to castle when rook has already moved" in {
//    val pos = whiteKingStartPos
//    val king = Piece(King, White)
//    val rook = Piece(Rook, White)
//    val state = GameState(
//      size = Vec2d(8, 8),
//      pieces = Map(pos -> king, Vec2d(0, 0) -> rook),
//      moveHistory = Vector(
//        Move(rook, Vec2d(0, 0), Vec2d(3, 0)),
//        Move(rook, Vec2d(3, 0), Vec2d(0, 0))
//      )
//    )
//    val normalMoves =
//      Set(Vec2d(3, 0), Vec2d(5, 0), Vec2d(3, 1), Vec2d(4, 1), Vec2d(5, 1))
//    val expected = normalMoves
//
//    PossibleMoves.getMoveTiles(pos, state).map(_._1).toSet shouldEqual expected
//  }
//
//  "king" should "not be able to castle when there are pieces between" in {
//    val pos = whiteKingStartPos
//    val piece = Piece(King, White)
//    val state = GameState(
//      size = Vec2d(8, 8),
//      pieces = Map(
//        pos -> piece,
//        Vec2d(0, 0) -> Piece(Rook, White),
//        Vec2d(1, 0) -> Piece(Bishop, White)
//      )
//    )
//    val normalMoves =
//      Set(Vec2d(3, 0), Vec2d(5, 0), Vec2d(3, 1), Vec2d(4, 1), Vec2d(5, 1))
//    val expected = normalMoves
//
//    PossibleMoves.getMoveTiles(pos, state).map(_._1).toSet shouldEqual expected
//  }
//
//  "king" should "not be able to castle when is checked" in {
//    val pos = whiteKingStartPos
//    val piece = Piece(King, White)
//    val state = GameState(
//      size = Vec2d(8, 8),
//      pieces = Map(
//        pos -> piece,
//        Vec2d(0, 0) -> Piece(Rook, White),
//        Vec2d(whiteKingStartPos.x, 7) -> Piece(Rook, Black)
//      )
//    )
//    val expected = Set(Vec2d(3, 0), Vec2d(5, 0), Vec2d(3, 1), Vec2d(5, 1))
//
//    PossibleMoves.getMoveTiles(pos, state).map(_._1).toSet shouldEqual expected
//  }
//
//  "king" should "not be able to castle when is check on path to castle" in {
//    val pos = whiteKingStartPos
//    val piece = Piece(King, White)
//    val state = GameState(
//      size = Vec2d(8, 8),
//      pieces = Map(
//        pos -> piece,
//        Vec2d(0, 0) -> Piece(Rook, White),
//        Vec2d(whiteKingStartPos.x - 1, 7) -> Piece(Rook, Black)
//      )
//    )
//    val expected = Set(Vec2d(5, 0), Vec2d(4, 1), Vec2d(5, 1))
//
//    PossibleMoves.getMoveTiles(pos, state).map(_._1).toSet shouldEqual expected
//  }
//
//  "king" should "be able to castle" in {
//    val pos = whiteKingStartPos
//    val piece = Piece(King, White)
//    val state = GameState(
//      size = Vec2d(8, 8),
//      pieces = Map(pos -> piece, Vec2d(0, 0) -> Piece(Rook, White))
//    )
//    val normalMoves =
//      Set(Vec2d(3, 0), Vec2d(5, 0), Vec2d(3, 1), Vec2d(4, 1), Vec2d(5, 1))
//    val expected = normalMoves ++ Set(Vec2d(2, 0))
//
//    PossibleMoves.getMoveTiles(pos, state).map(_._1).toSet shouldEqual expected
//  }
//}

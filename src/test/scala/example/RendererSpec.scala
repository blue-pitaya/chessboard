package example

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import example.models.Vec2d
import example.models.HexColor
import scala.scalajs.js.JSConverters._

class RendererSpec extends AnyFlatSpec with Matchers {
  val darkColor = HexColor("#000000")
  val lightColor = HexColor("#ffffff")
  val someBoard = Board(
    tiles = Map(
      Vec2d(0, 0) -> Tile(color = darkColor),
      Vec2d(1, 0) -> Tile(color = lightColor),
      Vec2d(0, 1) -> Tile(color = lightColor),
      Vec2d(1, 1) -> Tile(color = darkColor)
    ),
    size = Vec2d(2, 2)
  )

  "getTiles" should "be ok" in {
    val colorset = TileColorset(darkColor, lightColor)
    val size = Vec2d(2, 2)
    val expected = someBoard

    Renderer.getTiles(size, colorset) shouldEqual expected
  }

  "renderTiles" should "be ok" in {
    val totalSizeInPx = Vec2d(200, 200)
    val expectedSize = Vec2d(100, 100)
    val expected = Set(
      TileObj(
        position = Vec2d(0, 0),
        size = expectedSize,
        color = lightColor.value,
        rankMark = Some("2").orUndefined,
        fileMark = None.orUndefined
      ),
      TileObj(
        position = Vec2d(100, 0),
        size = expectedSize,
        color = darkColor.value,
        rankMark = None.orUndefined,
        fileMark = None.orUndefined
      ),
      TileObj(
        position = Vec2d(0, 100),
        size = expectedSize,
        color = darkColor.value,
        rankMark = Some("1").orUndefined,
        fileMark = Some("a").orUndefined
      ),
      TileObj(
        position = Vec2d(100, 100),
        size = expectedSize,
        color = lightColor.value,
        rankMark = None.orUndefined,
        fileMark = Some("b").orUndefined,
      )
    )

    Renderer.renderBoard(totalSizeInPx, someBoard) shouldEqual expected
  }
}

package example

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import example.models.Vec2d
import example.models.Color

class RendererSpec extends AnyFlatSpec with Matchers {
  "render tiles" should "be ok" in {
    val darkColor = Color(0, 0, 0)
    val lightColor = Color(255, 255, 255)
    val colorset = TileColorset(darkColor, lightColor)
    val size = Vec2d(2, 2)
    val expected = Map(
      Vec2d(0, 0) -> Tile(color = darkColor),
      Vec2d(1, 0) -> Tile(color = lightColor),
      Vec2d(0, 1) -> Tile(color = lightColor),
      Vec2d(1, 1) -> Tile(color = darkColor)
    )

    Renderer.renderTiles(size, colorset) shouldEqual expected
  }
}

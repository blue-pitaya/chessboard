package example

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import java.awt.Color
import example.models.Vec2d

class RendererSpec extends AnyFlatSpec with Matchers {
  "render tiles" should "be ok" in {
    val darkColor = new Color(0, 0, 0)
    val lightColor = new Color(255, 255, 255)
    val colorset = TileColorset(darkColor, lightColor)
    val size = Vec2d(2, 2)
    val expected = Map(
      Vec2d(0, 0) -> darkColor,
      Vec2d(1, 0) -> lightColor,
      Vec2d(0, 1) -> darkColor,
      Vec2d(1, 1) -> lightColor
    )

    Renderer.renderTiles(size, colorset) shouldEqual expected
  }

  "fail" should "fail" in {
    true shouldEqual false
  }
}

export interface Vec2d {
  x: number;
  y: number;
}

export const vec2dAsString = (v: Vec2d): string =>
  v.x.toString() + "_" + v.y.toString();

export interface Tile {
  position: Vec2d;
  size: Vec2d;
  color: string;
}

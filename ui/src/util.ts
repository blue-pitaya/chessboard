export interface Vec2d {
  x: number;
  y: number;
}

export const vec2dAsString = (v: Vec2d): string =>
  v.x.toString() + "_" + v.y.toString();

//TODO: this is Tile proxy (dont mutate outside scala)
export interface Tile {
  id: string;
  position: Vec2d;
  size: Vec2d;
  color: string;
  isHighlighted: boolean;
  fileMark?: string;
  rankMark?: string;
}

//TODO: this is Piece proxy
export interface Piece {
  id: string;
  position: Vec2d;
  size: Vec2d;
  pieceColor: string;
  pieceKind: string;
}

export const pieceToImageFilename = (piece: Piece): string => {
  const color = piece.pieceColor.toLowerCase();
  const kind = piece.pieceKind.toLowerCase();

  return `${color}-${kind}.png`;
};

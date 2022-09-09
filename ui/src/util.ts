export interface Vec2d {
  x: number;
  y: number;
}

export const normalizeVec2d = (v: Vec2d): Vec2d => ({
  x: Math.floor(v.x),
  y: Math.floor(v.y),
});

//TODO: this is Tile proxy (dont mutate outside scala)
//TODO: move it to state dir
export interface Tile {
  id: string;
  position: Vec2d;
  size: Vec2d;
  color: string;
  fileMark?: string;
  rankMark?: string;
  isMarked: boolean;
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

<script lang="ts" setup>
import { renderTiles, renderPieces } from "@/scalajs/main";
import { Tile, vec2dAsString, Piece, pieceToImageFilename } from "@/util";
import TileComp from "./TileComp.vue";
import PieceComp from "./PieceComp.vue";

const tiles: Array<Tile> = renderTiles();
const pieces: Array<Piece> = renderPieces();
</script>

<template>
  <svg>
    <TileComp
      v-for="t in tiles"
      :key="vec2dAsString(t.position)"
      :position="t.position"
      :size="t.size"
      :color="t.color"
      :file-mark="t.fileMark"
      :rank-mark="t.rankMark"
    />
    <PieceComp
      v-for="p in pieces"
      :key="vec2dAsString(p.position)"
      :position="p.position"
      :size="p.size"
      :image-filename="pieceToImageFilename(p)"
    />
  </svg>
</template>

<style scoped>
svg {
  width: 800px;
  height: 800px;
}
</style>

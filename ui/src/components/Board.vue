<script lang="ts" setup>
import { renderTiles } from "@/scalajs/main";
import { Tile, vec2dAsString } from "@/util";
import TileComp from "./TileComp.vue";

const tiles: Array<Tile> = renderTiles();
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
  </svg>
</template>

<style scoped>
svg {
  width: 800px;
  height: 800px;
}
</style>

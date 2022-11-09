<script lang="ts" setup>
import Board from "@/components/Board.vue";
</script>

<template>
  <p>
    Simple chessborad, with implementes chess rules (without turn restrictions).
    Todo: promoting pawn, rewriting code to Cats Effect, some fancy featureas
    like playing actual game xd.
  </p>
  <div class="centerElement">
    <Board />
  </div>
</template>

<style scoped>
.centerElement {
  display: flex;
  width: 100%;
  height: 100%;
  justify-content: center;
  align-items: center;
}
</style>

import { ref } from "vue";
import { Piece, Tile } from "../util";
import { getState } from "../scalajs/main";

export interface State {
  tiles: Array<Tile>;
  pieces: Array<Piece>;
}

export const useState = () => {
  const state = ref<State>(getState());

  const updateState = (updated: State) => {
    state.value = updated;
  };

  return {
    state,
    updateState,
  };
};

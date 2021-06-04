import * as React from "react";
import { KeyboardEventHandler } from "react";

interface KeyHandlersByKey {
  [key: string]: KeyboardEventHandler;
}

export const useOnKeyDown = (
  handlers: KeyHandlersByKey,
): KeyboardEventHandler => {
  return React.useCallback(
    (e: React.KeyboardEvent) => {
      const handler = handlers[e.key];

      if (handler) handler(e);
    },
    [handlers],
  );
};

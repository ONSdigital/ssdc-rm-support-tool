// jest-dom adds custom matchers for asserting on DOM nodes.
// allows you to do things like:
// expect(element).toHaveTextContent(/react/i)
// learn more: https://github.com/testing-library/jest-dom
import "@testing-library/jest-dom/vitest";
import { afterEach, beforeEach, vi } from "vitest";

// JSS (used by Material-UI v4) grabs a detached reference to CSS.escape, which
// jsdom rejects because its implementation brand checks `this`. Real browsers
// don't, so bind it here to keep the detached reference working under test.
if (typeof CSS !== "undefined" && typeof CSS.escape === "function") {
  CSS.escape = CSS.escape.bind(CSS);
}

// Components call the API on mount, and Node's fetch can't resolve the relative
// URLs they use. Default every test to a stub returning an empty JSON response;
// tests that care about the response should override fetch themselves.
beforeEach(() => {
  vi.stubGlobal(
    "fetch",
    vi.fn(() =>
      Promise.resolve(
        new Response("[]", {
          status: 200,
          headers: { "Content-Type": "application/json" },
        }),
      ),
    ),
  );
});

afterEach(() => {
  vi.unstubAllGlobals();
});

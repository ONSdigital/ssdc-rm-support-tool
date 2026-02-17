import React from "react";
import { render, screen } from "@testing-library/react";
import App from "./App";

test("renders toolbar title", () => {
  render(<App />);
  const titleElement = screen.getByText(/RM Support Tool/i);
  expect(titleElement).toBeInTheDocument();
});

import React from "react";
import {
  render,
  fireEvent,
  waitFor,
  waitForElementToBeRemoved,
} from "@testing-library/react";
import "@testing-library/jest-dom/extend-expect";
import HomePage from "./HomePage";
import { Provider } from "react-redux";
import { createStore } from "redux";
import authReducer from "../redux/authReducer";
import * as apiCalls from "../api/apiCalls";
import { BrowserRouter } from "react-router-dom";

const defaultState = {
  id: 1,
  username: "user1",
  displayName: "display1",
  image: "image1.png",
  password: "P4ssword",
  isLoggedIn: true,
};

let store;

const setup = (state = defaultState) => {
  store = createStore(authReducer, state);
  return render(
    <Provider store={store}>
      <BrowserRouter>
        <HomePage />
      </BrowserRouter>
    </Provider>
  );
};

apiCalls.loadPosts = jest.fn().mockResolvedValue({
  data: {
    content: [],
    number: 0,
    size: 3,
  },
});

describe("HomePage", () => {
  describe("Layout", () => {
    it("has root page div", () => {
      const { queryByTestId } = setup();
      const homePageDiv = queryByTestId("homepage");
      expect(homePageDiv).toBeInTheDocument();
    });

    it("does not display PostSubmit when user not logged in", () => {
      const notLoggedInState = {
        id: 0,
        username: "",
        displayName: "",
        image: "",
        password: "",
        isLoggedIn: false,
      };
      const { container } = setup(notLoggedInState);
      const textArea = container.querySelector("textarea");
      expect(textArea).not.toBeInTheDocument();
    });
  });
});

console.error = () => { };

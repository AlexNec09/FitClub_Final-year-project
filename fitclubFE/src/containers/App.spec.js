import React from "react";
import { render, fireEvent, waitFor, queryAllByText } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import App from "./App";
import { Provider } from "react-redux";
import configureStore from "../redux/configureStore";
import axios from "axios";
import * as apiCalls from "../api/apiCalls";

beforeEach(() => {
  localStorage.clear();
  delete axios.defaults.headers.common["Authorization"];
});

apiCalls.listUsers = jest.fn().mockResolvedValue({
  data: {
    content: [],
    number: 0,
    size: 3,
  },
});

apiCalls.getUser = jest.fn().mockResolvedValue({
  data: {
    id: 1,
    username: "user1",
    displayName: "display1",
    image: "profile1.png",
  },
});

apiCalls.loadMessages = jest.fn().mockResolvedValue({
  data: {
    content: [],
    number: 0,
    size: 3,
  },
});

const mockSuccessGetUser1 = {
  data: {
    id: 1,
    username: "user1",
    displayName: "display1",
    image: "profile1.png",
  },
};

const mockSuccessGetUser2 = {
  data: {
    id: 2,
    username: "user2",
    displayName: "display2",
    image: "profile2.png",
  },
};

const mockFailGetUser = {
  response: {
    data: {
      message: "User not found",
    },
  },
};

const setup = (path) => {
  const store = configureStore(false);
  return render(
    <Provider store={store}>
      <MemoryRouter initialEntries={[path]}>
        <App />
      </MemoryRouter>
    </Provider>
  );
};

const changeEvent = (content) => {
  return {
    target: {
      value: content,
    },
  };
};

const setUserOneLoggedInStorage = () => {
  localStorage.setItem(
    "fitClub-auth",
    JSON.stringify({
      id: 1,
      username: "user1",
      displayName: "displayName1",
      image: "image1.png",
      password: "P4ssword",
      isLoggedIn: true,
      jwt: "test-jwt-token"
    })
  );
};

describe("App", () => {
  it("displays HomePage when url is /", () => {
    const { queryByTestId } = setup("/");
    expect(queryByTestId("homepage")).toBeInTheDocument();
  });

  it("displays userpage when url  other than /, /login or /signup", () => {
    const { queryByTestId } = setup("/user1");
    expect(queryByTestId("userpage")).toBeInTheDocument();
  });

  it("shows the UserSignupPage when clicking signup", () => {
    const { queryAllByText, container } = setup("/");
    const signupLink = queryAllByText("Sign Up")[0];
    fireEvent.click(signupLink);
    const header = container.querySelector("h2");
    expect(header).toHaveTextContent("Create Account");
  });

  it("displays UserSignupPage when url is /signup", () => {
    const { container } = setup("/signup");
    const header = container.querySelector("h2");
    expect(header).toHaveTextContent("Create Account");
  });

  it("shows the LoginPage when clicking login", () => {
    const { queryAllByText, container } = setup("/");
    const loginLink = queryAllByText("Login")[0];
    fireEvent.click(loginLink);
    const header = container.querySelector("h2");
    expect(header).toHaveTextContent("Welcome!");
  });

  it("displays LoginPage when url is /login", () => {
    const { container } = setup("/login");
    const header = container.querySelector("h2");
    expect(header).toHaveTextContent("Welcome!");
  });

  it("displays topBar when url is /", () => {
    const { container } = setup("/");
    const navigation = container.querySelector("nav");
    expect(navigation).toBeInTheDocument();
  });

  it("displays topBar when url is /signup", () => {
    const { container } = setup("/signup");
    const navigation = container.querySelector("nav");
    expect(navigation).toBeInTheDocument();
  });

  it("displays topBar when url is /login", () => {
    const { container } = setup("/login");
    const navigation = container.querySelector("nav");
    expect(navigation).toBeInTheDocument();
  });

  it("displays topBar when url is /user1", () => {
    const { container } = setup("/user1");
    const navigation = container.querySelector("nav");
    expect(navigation).toBeInTheDocument();
  });

  it("shows the HomePage when clicking the logo", () => {
    const { queryByTestId, container } = setup("/login");
    const logo = container.querySelector("img");
    fireEvent.click(logo);
    expect(queryByTestId("homepage")).toBeInTheDocument();
  });

  it("displays My Profile on TopBar after login success", async () => {
    const { container, queryByText } = setup("/login");
    const usernameInput = container.querySelector(`input[name="username"]`);
    fireEvent.change(usernameInput, changeEvent("user1"));
    const passwordInput = container.querySelector(`input[name="password"]`);
    fireEvent.change(passwordInput, changeEvent("P4ssword"));
    const button = container.querySelector("button");

    axios.post = jest.fn().mockResolvedValue({
      data: {
        id: 1,
        username: "user1",
        displayName: "displayName1",
        image: "image1.png",
      },
    });
    fireEvent.click(button);
    await waitFor(() => expect(queryByText("My Profile")).toBeInTheDocument());
  });

  it("displays My Profile on TopBar after signup success", async () => {
    const { queryByPlaceholderText, container, queryByText } = setup("/signup");

    const displayNameInput = queryByPlaceholderText("Your display name");
    const userNameInput = container.querySelector(`input[name="username"]`);
    const passwordInput = container.querySelector(`input[name="password"]`);
    const passwordRepeat = queryByPlaceholderText("Repeat your password");

    fireEvent.change(displayNameInput, changeEvent("display1"));
    fireEvent.change(userNameInput, changeEvent("user1"));
    fireEvent.change(passwordInput, changeEvent("P4ssword"));
    fireEvent.change(passwordRepeat, changeEvent("P4ssword"));

    const button = container.querySelector("button");

    axios.post = jest
      .fn()
      .mockResolvedValueOnce({
        data: {
          message: "User saved",
        },
      })
      .mockResolvedValueOnce({
        data: {
          id: 1,
          username: "user1",
          displayName: "displayName1",
          image: "image1.png",
        },
      });
    fireEvent.click(button);
    await waitFor(() => expect(queryByText("My Profile")).toBeInTheDocument());
  });

  it("saves logged in user data to localStorage after login success", async () => {
    const { queryByPlaceholderText, container, queryByText } = setup("/login");
    const usernameInput = container.querySelector(`input[name="username"]`);
    fireEvent.change(usernameInput, changeEvent("user1"));
    const passwordInput = container.querySelector(`input[name="password"]`);
    fireEvent.change(passwordInput, changeEvent("P4ssword"));
    const button = container.querySelector("button");

    axios.post = jest.fn().mockResolvedValue({
      data: {
        id: 1,
        username: "user1",
        displayName: "displayName1",
        image: "image1.png",
      },
    });
    fireEvent.click(button);
    await waitFor(() => queryByText("My Profile"));
    const dataInStorage = JSON.parse(localStorage.getItem("fitClub-auth"));
    expect(dataInStorage).toEqual({
      id: 1,
      username: "user1",
      displayName: "displayName1",
      image: "image1.png",
      password: "P4ssword",
      isLoggedIn: true,
    });
  });

  it("displays logged in topBar when storage has logged in user data", async () => {
    setUserOneLoggedInStorage();
    const { queryAllByText } = setup("/");
    const myProfileLink = queryAllByText("My Profile")[0];
    expect(myProfileLink).toBeInTheDocument();
  });

  it("set axios authorization with base64 encoded user credentials after login success", async () => {
    const { queryByPlaceholderText, container, queryByText } = setup("/login");
    const usernameInput = container.querySelector(`input[name="username"]`);
    fireEvent.change(usernameInput, changeEvent("user1"));
    const passwordInput = container.querySelector(`input[name="password"]`);
    fireEvent.change(passwordInput, changeEvent("P4ssword"));
    const button = container.querySelector("button");

    axios.post = jest.fn().mockResolvedValue({
      data: {
        id: 1,
        username: "user1",
        displayName: "displayName1",
        image: "image1.png",
      },
    });
    fireEvent.click(button);
    await waitFor(() => queryByText("My Profile"));
    const axiosAuthorization = axios.defaults.headers.common["Authorization"];

    const encoded = btoa("user1:P4ssword");
    //const encoded = encodeURIComponent('user1:P4ssword');

    const expectedAuthorization = `Basic ${encoded}`;
    expect(axiosAuthorization).toBe(expectedAuthorization);
  });

  it("set axios authorization with base64 encoded user credentials when storage has logged in user data", () => {
    setUserOneLoggedInStorage();
    setup("/");
    const axiosAuthorization = axios.defaults.headers.common["Authorization"];
    const encoded = btoa("user1:P4ssword");
    const expectedAuthorization = `Basic ${encoded}`;

    expect(axiosAuthorization).toBe(expectedAuthorization);
  });

  it("removes axios authorization header when user logout", () => {
    setUserOneLoggedInStorage();
    const { queryByText } = setup("/");
    fireEvent.click(queryByText("Logout"));
    const axiosAuthorization = axios.defaults.headers.common["Authorization"];

    expect(axiosAuthorization).toBeFalsy();
  });

  it("updates user page after clicking my profile when another user page was opened", async () => {
    apiCalls.getUser = jest
      .fn()
      .mockResolvedValueOnce(mockSuccessGetUser1)
      .mockResolvedValueOnce(mockSuccessGetUser2);

    setUserOneLoggedInStorage();
    const { queryAllByText, queryByText } = setup("/user2");
    await waitFor(() => queryByText("display2@user2"));
    const myProfileLink = queryAllByText("My Profile")[0];
    fireEvent.click(myProfileLink);
    waitFor(() => expect(queryByText("display1")).toBeInTheDocument()); // Good test
  });

  it("updates page after clicking my profile when another non existing user page was opened", async () => {
    apiCalls.getUser = jest
      .fn()
      .mockRejectedValue(mockFailGetUser)
      .mockResolvedValueOnce(mockSuccessGetUser2);
    setUserOneLoggedInStorage();

    const { queryAllByText, queryByText } = setup("/user50");
    await waitFor(() => queryByText("User not found"));
    const myProfileLink = queryAllByText("My Profile")[0];
    fireEvent.click(myProfileLink);
    waitFor(() => expect(queryByText("display1")).toBeInTheDocument()); // Good test
  });
});

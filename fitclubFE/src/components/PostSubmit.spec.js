import React from "react";
import {
  render,
  fireEvent,
  waitFor,
  waitForElementToBeRemoved,
} from "@testing-library/react";
import { Provider } from "react-redux";
import { createStore } from "redux";
import authReducer from "../redux/authReducer";
import PostSubmit from "./PostSubmit";
import * as apiCalls from "../api/apiCalls";
import createJWKSMock from "mock-jwks";

const defaultState = {
  id: 1,
  username: "user1",
  displayName: "display1",
  image: "profile1.png",
  password: "P4ssword",
  isLoggedIn: true,
};

let store;

const setup = (state = defaultState) => {
  store = createStore(authReducer, state);
  return render(
    <Provider store={store}>
      <PostSubmit />
    </Provider>
  );
};

describe("PostSubmit", () => {
  const jwks = createJWKSMock("http://localhost:3000/");

  beforeEach(() => {
    jwks.start();
  });

  afterEach(() => {
    jwks.stop();
  });

  describe("Layout", () => {
    it("has textarea", () => {
      const { container } = setup();
      const textArea = container.querySelector("textarea");
      expect(textArea).toBeInTheDocument();
    });

    it("has image", () => {
      const { container } = setup();
      const image = container.querySelector("img");
      expect(image).toBeInTheDocument();
    });

    it("has textarea", () => {
      const { container } = setup();
      const textArea = container.querySelector("textarea");
      expect(textArea.rows).toBe(1);
    });

    it("displays user image", () => {
      const { container } = setup();
      const image = container.querySelector("img");
      expect(image.src).toContain("/images/profile/" + defaultState.image);
    });
  });
  describe("Interactions", () => {
    let textArea;
    const setupFocused = (state = defaultState) => {
      const rendered = setup(state);
      textArea = rendered.container.querySelector("textarea");
      fireEvent.focus(textArea);
      return rendered;
    };

    it("displays 3 rows when focused to textarea", () => {
      setupFocused();
      expect(textArea.rows).toBe(3);
    });

    it("displays send post button when focused to textarea", () => {
      const { queryByText } = setupFocused();
      const sendButton = queryByText("Send");
      expect(sendButton).toBeInTheDocument();
    });

    it("displays Cancel button when focused to textarea", () => {
      const { queryByText } = setupFocused();
      const cancelButton = queryByText("Cancel");
      expect(cancelButton).toBeInTheDocument();
    });

    it("does not display Send button when not focused to textarea", () => {
      const { queryByText } = setup();
      const sendButton = queryByText("Send");
      expect(sendButton).not.toBeInTheDocument();
    });

    it("does not display Cancel button when not focused to textarea", () => {
      const { queryByText } = setup();
      const cancelButton = queryByText("Cancel");
      expect(cancelButton).not.toBeInTheDocument();
    });

    it("returns back to unfocused state after clicking the cancel", () => {
      const { queryByText } = setupFocused();
      const cancelButton = queryByText("Cancel");
      fireEvent.click(cancelButton);
      expect(queryByText("Cancel")).not.toBeInTheDocument();
    });

    it("calls postUserPost with post request object when clicking Send", () => {
      const token = jwks.token({});
      const stateWithMockJwt = Object.assign(defaultState, { jwt: token });

      const { queryByText } = setupFocused(stateWithMockJwt);
      fireEvent.change(textArea, { target: { value: "Test post content" } });

      const sendButton = queryByText("Send");

      apiCalls.postUserPost = jest.fn().mockResolvedValue({});
      fireEvent.click(sendButton);

      expect(apiCalls.postUserPost).toHaveBeenCalledWith({
        content: "Test post content",
      }, token);
    });

    it("returns back to unfocused state after successful postUserPost action", async () => {
      const { queryByText } = setupFocused();
      fireEvent.change(textArea, { target: { value: "Test post content" } });

      const sendButton = queryByText("Send");

      apiCalls.postUserPost = jest.fn().mockResolvedValue({});
      fireEvent.click(sendButton);

      await waitFor(() => {
        expect(queryByText("Send")).not.toBeInTheDocument();
      });
    });

    it("clear content after successful postUserPost action", async () => {
      const { queryByText } = setupFocused();
      fireEvent.change(textArea, { target: { value: "Test post content" } });

      const sendButton = queryByText("Send");

      apiCalls.postUserPost = jest.fn().mockResolvedValue({});
      fireEvent.click(sendButton);

      await waitFor(() => {
        expect(queryByText("Test post content")).not.toBeInTheDocument();
      });
    });

    it("clears content after clicking cancel", () => {
      const { queryByText } = setupFocused();
      fireEvent.change(textArea, { target: { value: "Test post content" } });

      fireEvent.click(queryByText("Cancel"));

      expect(queryByText("Test post content")).not.toBeInTheDocument();
    });

    it("disables Send button when there is postUserPost api call", async () => {
      const { queryByText } = setupFocused();
      fireEvent.change(textArea, { target: { value: "Test post content" } });

      const sendButton = queryByText("Send");

      const mockFunction = jest.fn().mockImplementation(() => {
        return new Promise((resolve, reject) => {
          setTimeout(() => {
            resolve({});
          }, 300);
        });
      });

      apiCalls.postUserPost = mockFunction;
      fireEvent.click(sendButton);

      fireEvent.click(sendButton);
      expect(mockFunction).toHaveBeenCalledTimes(1);
    });

    it("disables Cancel button when there is postUserPost api call", async () => {
      const { queryByText } = setupFocused();
      fireEvent.change(textArea, { target: { value: "Test post content" } });

      const sendButton = queryByText("Send");

      const mockFunction = jest.fn().mockImplementation(() => {
        return new Promise((resolve, reject) => {
          setTimeout(() => {
            resolve({});
          }, 300);
        });
      });

      apiCalls.postUserPost = mockFunction;
      fireEvent.click(sendButton);

      const cancelButton = queryByText("Cancel");
      expect(cancelButton).toBeDisabled();
    });

    it("displays spinner when there is postUserPost api call", async () => {
      const { queryByText, queryByRole } = setupFocused();
      fireEvent.change(textArea, { target: { value: "Test post content" } });

      const sendButton = queryByText("Send");

      const mockFunction = jest.fn().mockImplementation(() => {
        return new Promise((resolve, reject) => {
          setTimeout(() => {
            resolve({});
          }, 300);
        });
      });

      apiCalls.postUserPost = mockFunction;
      fireEvent.click(sendButton);

      const spinner = queryByRole("status");
      expect(spinner).toBeInTheDocument();
    });

    it("enables Send button when postUserPost api call fails", async () => {
      const { queryByText } = setupFocused();
      fireEvent.change(textArea, { target: { value: "Test post content" } });

      const sendButton = queryByText("Send");

      const mockFunction = jest.fn().mockRejectedValueOnce({
        response: {
          data: {
            validationErrors: {
              content: "It must have minimum 10 and maximum 5000 characters",
            },
          },
        },
      });

      apiCalls.postUserPost = mockFunction;
      fireEvent.click(sendButton);

      await waitFor(() => {
        expect(queryByText("Send")).not.toBeDisabled();
      });
    });

    it("enables Cancel button when postUserPost api call fails", async () => {
      const { queryByText } = setupFocused();
      fireEvent.change(textArea, { target: { value: "Test post content" } });

      const sendButton = queryByText("Send");

      const mockFunction = jest.fn().mockRejectedValueOnce({
        response: {
          data: {
            validationErrors: {
              content: "It must have minimum 10 and maximum 5000 characters",
            },
          },
        },
      });

      apiCalls.postUserPost = mockFunction;
      fireEvent.click(sendButton);

      await waitFor(() => {
        expect(queryByText("Cancel")).not.toBeDisabled();
      });
    });

    it("enables Send button after successful postUserPost action", async () => {
      const { queryByText } = setupFocused();
      fireEvent.change(textArea, { target: { value: "Test post content" } });

      const sendButton = queryByText("Send");

      apiCalls.postUserPost = jest.fn().mockResolvedValue({});
      fireEvent.click(sendButton);

      await waitForElementToBeRemoved(sendButton);

      fireEvent.focus(textArea);
      await waitFor(() => {
        expect(queryByText("Send")).not.toBeDisabled();
      });
    });

    it("displays validation error for content", async () => {
      const { queryByText } = setupFocused();
      fireEvent.change(textArea, { target: { value: "Test post content" } });

      const sendButton = queryByText("Send");

      const mockFunction = jest.fn().mockRejectedValueOnce({
        response: {
          data: {
            validationErrors: {
              content: "It must have minimum 10 and maximum 5000 characters",
            },
          },
        },
      });

      apiCalls.postUserPost = mockFunction;
      fireEvent.click(sendButton);

      await waitFor(() => {
        expect(
          queryByText("It must have minimum 10 and maximum 5000 characters")
        ).toBeInTheDocument();
      });
    });

    it("clears validation error after clicking cancel", async () => {
      const { queryByText, findByText } = setupFocused();
      fireEvent.change(textArea, { target: { value: "Test post content" } });

      const sendButton = queryByText("Send");

      const mockFunction = jest.fn().mockRejectedValueOnce({
        response: {
          data: {
            validationErrors: {
              content: "It must have minimum 10 and maximum 5000 characters",
            },
          },
        },
      });

      apiCalls.postUserPost = mockFunction;
      fireEvent.click(sendButton);

      const error = await findByText(
        "It must have minimum 10 and maximum 5000 characters"
      );

      fireEvent.click(queryByText("Cancel"));

      expect(error).not.toBeInTheDocument();
    });

    it("clears validation error after content is changed", async () => {
      const { queryByText, findByText } = setupFocused();
      fireEvent.change(textArea, { target: { value: "Test post content" } });

      const sendButton = queryByText("Send");

      const mockFunction = jest.fn().mockRejectedValueOnce({
        response: {
          data: {
            validationErrors: {
              content: "It must have minimum 10 and maximum 5000 characters",
            },
          },
        },
      });

      apiCalls.postUserPost = mockFunction;
      fireEvent.click(sendButton);
      const error = await findByText(
        "It must have minimum 10 and maximum 5000 characters"
      );

      fireEvent.change(textArea, {
        target: { value: "Test post content updated" },
      });

      expect(error).not.toBeInTheDocument();
    });

    it("displays file attachment input when text area focused", () => {
      const { container } = setup();
      const textArea = container.querySelector("textarea");
      fireEvent.focus(textArea);

      const uploadInput = container.querySelector("input");
      expect(uploadInput.type).toBe("file");
    });

    it("displays image component when file selected", async () => {
      apiCalls.postUserPostFile = jest.fn().mockResolvedValue({
        data: {
          id: 1,
          name: "random-name.png",
        },
      });

      const { container } = setup();
      const textArea = container.querySelector("textarea");
      fireEvent.focus(textArea);

      const uploadInput = container.querySelector("input");
      expect(uploadInput.type).toBe("file");

      const file = new File(["dummy content"], "example.png", {
        type: "image/png",
      });
      fireEvent.change(uploadInput, { target: { files: [file] } });

      await waitFor(() => {
        const images = container.querySelectorAll("img");
        const attachmentImage = images[1];
        expect(attachmentImage.src).toContain("data:image/png;base64");
      });
    });

    it("removes selected image after clicking cancel", async () => {
      apiCalls.postUserPostFile = jest.fn().mockResolvedValue({
        data: {
          id: 1,
          name: "random-name.png",
        },
      });

      const { queryByText, container } = setupFocused();

      const uploadInput = container.querySelector("input");
      expect(uploadInput.type).toBe("file");

      const file = new File(["dummy content"], "example.png", {
        type: "image/png",
      });
      fireEvent.change(uploadInput, { target: { files: [file] } });

      await waitFor(() => {
        const images = container.querySelectorAll("img");
        expect(images.length).toBe(2);
      });

      fireEvent.click(queryByText("Cancel"));
      fireEvent.focus(textArea);

      await waitFor(() => {
        const images = container.querySelectorAll("img");
        expect(images.length).toBe(1);
      });
    });

    it("calls postUserPostFile when file selected", async () => {
      apiCalls.postUserPostFile = jest.fn().mockResolvedValue({
        data: {
          id: 1,
          name: "random-name.png",
        },
      });

      const { container } = setupFocused();

      const uploadInput = container.querySelector("input");
      expect(uploadInput.type).toBe("file");

      const file = new File(["dummy content"], "example.png", {
        type: "image/png",
      });
      fireEvent.change(uploadInput, { target: { files: [file] } });

      await waitFor(() => {
        const images = container.querySelectorAll("img");
        expect(images.length).toBe(2);
      });
      expect(apiCalls.postUserPostFile).toHaveBeenCalledTimes(1);
    });

    it("calls postUserPostFile with selected file", async () => {
      apiCalls.postUserPostFile = jest.fn().mockResolvedValue({
        data: {
          id: 1,
          name: "random-name.png",
        },
      });

      const { container } = setupFocused();

      const uploadInput = container.querySelector("input");
      expect(uploadInput.type).toBe("file");

      const file = new File(["dummy content"], "example.png", {
        type: "image/png",
      });
      fireEvent.change(uploadInput, { target: { files: [file] } });

      await waitFor(() => {
        const images = container.querySelectorAll("img");
        expect(images.length).toBe(2);
      });

      const body = apiCalls.postUserPostFile.mock.calls[0][0];

      const readFile = () => {
        return new Promise((resolve, reject) => {
          const reader = new FileReader();

          reader.onloadend = () => {
            resolve(reader.result);
          };
          reader.readAsText(body.get("file"));
        });
      };

      const result = await readFile();

      expect(result).toBe("dummy content");
    });

    it("calls postUserPost with post with file attachment object when clicking Send", async () => {
      const token = jwks.token({});
      const stateWithMockJwt = Object.assign(defaultState, { jwt: token });

      apiCalls.postUserPostFile = jest.fn().mockResolvedValue({
        data: {
          id: 1,
          name: "random-name.png",
        },
      });
      const { queryByText, container } = setupFocused(stateWithMockJwt);
      fireEvent.change(textArea, { target: { value: "Test post content" } });

      const uploadInput = container.querySelector("input");
      expect(uploadInput.type).toBe("file");

      const file = new File(["dummy content"], "example.png", {
        type: "image/png",
      });
      fireEvent.change(uploadInput, { target: { files: [file] } });

      await waitFor(() => {
        const images = container.querySelectorAll("img");
        expect(images.length).toBe(2);
      });

      const sendButton = queryByText("Send");

      apiCalls.postUserPost = jest.fn().mockResolvedValue({});
      fireEvent.click(sendButton);

      expect(apiCalls.postUserPost).toHaveBeenCalledWith({
        attachment: {
          id: 1,
          name: "random-name.png",
        },
        content: "Test post content",
      }, token);
    });

    it("clears image after postUserPost success", async () => {
      apiCalls.postUserPostFile = jest.fn().mockResolvedValue({
        data: {
          id: 1,
          name: "random-name.png",
        },
      });
      const { queryByText, container } = setupFocused();
      fireEvent.change(textArea, { target: { value: "Test post content" } });

      const uploadInput = container.querySelector("input");
      expect(uploadInput.type).toBe("file");

      const file = new File(["dummy content"], "example.png", {
        type: "image/png",
      });
      fireEvent.change(uploadInput, { target: { files: [file] } });

      await waitFor(() => {
        const images = container.querySelectorAll("img");
        expect(images.length).toBe(2);
      });

      const sendButton = queryByText("Send");

      apiCalls.postUserPost = jest.fn().mockResolvedValue({});
      fireEvent.click(sendButton);

      fireEvent.focus(textArea);
      await waitFor(() => {
        const images = container.querySelectorAll("img");
        expect(images.length).toBe(1);
      });
    });

    it("calls postUserPost without file attachment after cancelling previous file selection", async () => {
      const token = jwks.token({});
      const stateWithMockJwt = Object.assign(defaultState, { jwt: token });

      apiCalls.postUserPostFile = jest.fn().mockResolvedValue({
        data: {
          id: 1,
          name: "random-name.png",
        },
      });
      const { queryByText, container } = setupFocused(stateWithMockJwt);
      fireEvent.change(textArea, { target: { value: "Test post content" } });

      const uploadInput = container.querySelector("input");
      expect(uploadInput.type).toBe("file");

      const file = new File(["dummy content"], "example.png", {
        type: "image/png",
      });
      fireEvent.change(uploadInput, { target: { files: [file] } });

      await waitFor(() => {
        const images = container.querySelectorAll("img");
        expect(images.length).toBe(2);
      });
      fireEvent.click(queryByText("Cancel"));
      fireEvent.focus(textArea);

      const sendButton = queryByText("Send");

      apiCalls.postUserPost = jest.fn().mockResolvedValue({});
      fireEvent.change(textArea, { target: { value: "Test post content" } });
      fireEvent.click(sendButton);

      expect(apiCalls.postUserPost).toHaveBeenCalledWith({
        content: "Test post content",
      }, token);
    });
  });
});

console.error = () => { };

import React from "react";
import {
  render,
  fireEvent,
  waitFor,
  waitForElementToBeRemoved,
  waitForDomChange,
} from "@testing-library/react";
import MessageFeed from "./MessageFeed";
import * as apiCalls from "../api/apiCalls";
import { MemoryRouter } from "react-router-dom";
import { Provider } from "react-redux";
import { createStore } from "redux";
import authReducer from "../redux/authReducer";

const loggedInStateUser1 = {
  id: 1,
  username: "user1",
  displayName: "display1",
  image: "profile1.png",
  password: "P4ssword",
  isLoggedIn: true,
};

const originalSetInterval = window.setInterval;
const originalClearInterval = window.clearInterval;

let timedFunction;
let mySetIntervalId = 123456;

const useFakeIntervals = () => {
  window.setInterval = (callback, interval) => {
    if (!callback.toString().startsWith("function")) {
      timedFunction = callback;
      return mySetIntervalId;
    }
  };
  window.clearInterval = (id) => {
    if (id === mySetIntervalId) {
      timedFunction = undefined;
    }
  };
};

const useRealIntervals = () => {
  window.setInterval = originalSetInterval;
  window.clearInterval = originalClearInterval;
};

const runTimer = () => {
  timedFunction && timedFunction();
};

const setup = (props, state = loggedInStateUser1) => {
  const store = createStore(authReducer, state);
  return render(
    <Provider store={store}>
      <MemoryRouter>
        <MessageFeed {...props} />
      </MemoryRouter>
    </Provider>
  );
};

const mockEmptyResponse = {
  data: {
    content: [],
  },
};

const mockResponseWithLoadedMessagePage = () => {
  return {
    data: {
      content: [
        {
          id: 15,
          content: 'This is the loaded message',
          date: new Date().getTime(),
          user: {
            username: 'user5',
            displayName: 'display5'
          },
          reactions: {
            likeCount: 5,
            dislikeCount: 7,
            loggedUserReaction: null
          }
        }
      ],
      number: 0,
      first: true,
      last: true,
      size: 5,
      totalPages: 1
    }
  }
};

const mockSuccessGetNewMessagesList = {
  data: [
    {
      id: 21,
      content: "This is the newest message",
      date: 1561294668539,
      user: {
        id: 1,
        username: "user1",
        displayName: "display1",
        image: "profile1.png",
      },
    },
  ],
};

const mockSuccessGetMessagesMiddleOfMultiPage = {
  data: {
    content: [
      {
        id: 5,
        content: "This message is in middle page",
        date: 1561294668539,
        user: {
          id: 1,
          username: "user1",
          displayName: "display1",
          image: "profile1.png",
        },
      },
    ],
    number: 0,
    first: false,
    last: false,
    size: 5,
    totalPages: 2,
  },
};

const mockSuccessGetMessagesSinglePage = {
  data: {
    content: [
      {
        id: 10,
        content: "This is the latest message",
        date: 1561294668539,
        user: {
          id: 1,
          username: "user1",
          displayName: "display1",
          image: "profile1.png",
        },
      },
    ],
    number: 0,
    first: true,
    last: true,
    size: 5,
    totalPages: 1,
  },
};

const mockSuccessGetMessagesFirstOfMultiPage = {
  data: {
    content: [
      {
        id: 10,
        content: "This is the latest message",
        date: 1561294668539,
        user: {
          id: 1,
          username: "user1",
          displayName: "display1",
          image: "profile1.png",
        },
      },
      {
        id: 9,
        content: "This is message 9",
        date: 1561294668539,
        user: {
          id: 1,
          username: "user1",
          displayName: "display1",
          image: "profile1.png",
        },
      },
    ],
    number: 0,
    first: true,
    last: false,
    size: 5,
    totalPages: 2,
  },
};

const mockSuccessGetMessagesLastOfMultiPage = {
  data: {
    content: [
      {
        id: 1,
        content: "This is the oldest message",
        date: 1561294668539,
        user: {
          id: 1,
          username: "user1",
          displayName: "display1",
          image: "profile1.png",
        },
      },
    ],
    number: 0,
    first: true,
    last: true,
    size: 5,
    totalPages: 2,
  },
};
describe("MessageFeed", () => {
  describe("Lifecycle", () => {
    it("calls loadMessages when it is rendered", () => {
      apiCalls.loadMessages = jest.fn().mockResolvedValue(mockEmptyResponse);
      setup();
      expect(apiCalls.loadMessages).toHaveBeenCalled();
    });

    it("calls loadMessages with user parameter when it is rendered with user property", () => {
      apiCalls.loadMessages = jest.fn().mockResolvedValue(mockEmptyResponse);
      setup({ user: "user1" });
      expect(apiCalls.loadMessages).toHaveBeenCalledWith("user1");
    });

    it("calls loadMessages without user parameter when it is rendered without user property", () => {
      apiCalls.loadMessages = jest.fn().mockResolvedValue(mockEmptyResponse);
      setup();
      const parameter = apiCalls.loadMessages.mock.calls[0][0];
      expect(parameter).toBeUndefined();
    });

    it("calls loadNewMessagesCount with topMessageId", async () => {
      jest.useFakeTimers();
      apiCalls.loadMessages = jest
        .fn()
        .mockResolvedValue(mockSuccessGetMessagesFirstOfMultiPage);
      apiCalls.loadNewMessagesCount = jest
        .fn()
        .mockResolvedValue({ data: { count: 1 } });
      const { findByText } = setup();
      await findByText("This is the latest message");
      jest.runOnlyPendingTimers();
      await findByText("There is 1 new message");
      const firstParam = apiCalls.loadNewMessagesCount.mock.calls[0][0];
      expect(firstParam).toBe(10);
      jest.useRealTimers();
    });

    it("calls loadNewMessagesCount with topMessageId and username when rendered with user property", async () => {
      jest.useFakeTimers();
      apiCalls.loadMessages = jest
        .fn()
        .mockResolvedValue(mockSuccessGetMessagesFirstOfMultiPage);
      apiCalls.loadNewMessagesCount = jest
        .fn()
        .mockResolvedValue({ data: { count: 1 } });
      const { findByText } = setup({ user: "user1" });
      await findByText("This is the latest message");
      jest.runOnlyPendingTimers();
      await findByText("There is 1 new message");
      expect(apiCalls.loadNewMessagesCount).toBeCalledWith(10, "user1");
      jest.useRealTimers();
    });

    it("displays new messages count as 1 after loadNewMessagesCount success", async () => {
      useFakeIntervals();
      apiCalls.loadMessages = jest
        .fn()
        .mockResolvedValue(mockSuccessGetMessagesFirstOfMultiPage);
      apiCalls.loadNewMessagesCount = jest
        .fn()
        .mockResolvedValue({ data: { count: 1 } });
      const { findByText } = setup({ user: "user1" });
      await findByText("This is the latest message");
      runTimer();
      const newMessagesCount = await findByText("There is 1 new message");
      expect(newMessagesCount).toBeInTheDocument();
      useRealIntervals();
    });

    it("displays new messages count constantly", async () => {
      useFakeIntervals();
      apiCalls.loadMessages = jest
        .fn()
        .mockResolvedValue(mockSuccessGetMessagesFirstOfMultiPage);
      apiCalls.loadNewMessagesCount = jest
        .fn()
        .mockResolvedValue({ data: { count: 1 } });
      const { findByText } = setup({ user: "user1" });
      await findByText("This is the latest message");
      runTimer();
      await findByText("There is 1 new message");
      apiCalls.loadNewMessagesCount = jest
        .fn()
        .mockResolvedValue({ data: { count: 2 } });
      runTimer();
      const newMessagesCount = await findByText("There are 2 new messages");
      expect(newMessagesCount).toBeInTheDocument();
      useRealIntervals();
    });

    it("does not call loadNewMessagesCount after component is unmounted", async () => {
      useFakeIntervals();
      apiCalls.loadMessages = jest
        .fn()
        .mockResolvedValue(mockSuccessGetMessagesFirstOfMultiPage);
      apiCalls.loadNewMessagesCount = jest
        .fn()
        .mockResolvedValue({ data: { count: 1 } });
      const { findByText, unmount } = setup({ user: "user1" });
      await findByText("This is the latest message");
      runTimer();
      await findByText("There is 1 new message");
      unmount();
      expect(apiCalls.loadNewMessagesCount).toHaveBeenCalledTimes(1);
      useRealIntervals();
    });

    it("displays new messages count as 1 after loadNewMessagesCount success when user does not have messages initially", async () => {
      useFakeIntervals();
      apiCalls.loadMessages = jest.fn().mockResolvedValue(mockEmptyResponse);
      apiCalls.loadNewMessagesCount = jest
        .fn()
        .mockResolvedValue({ data: { count: 1 } });
      const { findByText } = setup({ user: "user1" });
      await findByText("There are no messages");
      runTimer();
      const newMessagesCount = await findByText("There is 1 new message");
      expect(newMessagesCount).toBeInTheDocument();
      useRealIntervals();
    });
  });

  describe("Layout", () => {
    it("displays no message message when the response has empty page", async () => {
      apiCalls.loadMessages = jest.fn().mockResolvedValue(mockEmptyResponse);
      const { findByText } = setup();
      const message = await findByText("There are no messages");
      expect(message).toBeInTheDocument();
    });

    it("does not display no message message when the response has page of message", async () => {
      apiCalls.loadMessages = jest
        .fn()
        .mockResolvedValue(mockSuccessGetMessagesSinglePage);
      const { queryByText } = setup();
      const message = queryByText("There are no messages");
      await waitFor(() => {
        expect(message).not.toBeInTheDocument();
      });
    });

    it("displays spinner when loading the messages", async () => {
      apiCalls.loadMessages = jest.fn().mockImplementation(() => {
        return new Promise((resolve, reject) => {
          setTimeout(() => {
            resolve(mockSuccessGetMessagesSinglePage);
          }, 300);
        });
      });
      const { queryByRole } = setup();
      const spinner = queryByRole("status");
      expect(spinner).toBeInTheDocument();
    });

    it("displays message content", async () => {
      apiCalls.loadMessages = jest
        .fn()
        .mockResolvedValue(mockSuccessGetMessagesSinglePage);
      const { findByText } = setup();
      const messageContent = await findByText("This is the latest message");
      expect(messageContent).toBeInTheDocument();
    });

    it("displays View More Posts when there are next pages", async () => {
      apiCalls.loadMessages = jest
        .fn()
        .mockResolvedValue(mockSuccessGetMessagesFirstOfMultiPage);
      const { findByText } = setup();
      const loadMore = await findByText("View More Posts");
      expect(loadMore).toBeInTheDocument();
    });
  });

  describe("Interactions", () => {
    it("calls loadOldMessages with messageId when clicking View More Posts", async () => {
      apiCalls.loadMessages = jest
        .fn()
        .mockResolvedValue(mockSuccessGetMessagesFirstOfMultiPage);
      apiCalls.loadOldMessages = jest
        .fn()
        .mockResolvedValue(mockSuccessGetMessagesLastOfMultiPage);
      const { findByText } = setup();
      const loadMore = await findByText("View More Posts");
      fireEvent.click(loadMore);
      const firstParam = apiCalls.loadOldMessages.mock.calls[0][0];
      expect(firstParam).toBe(9);
    });

    it("calls loadOldMessages with messageId and username when clicking View More Posts when rendered with user property", async () => {
      apiCalls.loadMessages = jest
        .fn()
        .mockResolvedValue(mockSuccessGetMessagesFirstOfMultiPage);
      apiCalls.loadOldMessages = jest
        .fn()
        .mockResolvedValue(mockSuccessGetMessagesLastOfMultiPage);
      const { findByText } = setup({ user: "user1" });
      const loadMore = await findByText("View More Posts");
      fireEvent.click(loadMore);
      expect(apiCalls.loadOldMessages).toHaveBeenCalledWith(9, "user1");
    });

    it("displays loaded old message when loadOldMessages api call success", async () => {
      apiCalls.loadMessages = jest
        .fn()
        .mockResolvedValue(mockSuccessGetMessagesFirstOfMultiPage);
      apiCalls.loadOldMessages = jest
        .fn()
        .mockResolvedValue(mockSuccessGetMessagesLastOfMultiPage);
      const { findByText } = setup();
      const loadMore = await findByText("View More Posts");
      fireEvent.click(loadMore);
      const oldMessage = await findByText("This is the oldest message");
      expect(oldMessage).toBeInTheDocument();
    });

    it("hides View More Posts when loadOldMessages api call returns last page", async () => {
      apiCalls.loadMessages = jest
        .fn()
        .mockResolvedValue(mockSuccessGetMessagesFirstOfMultiPage);
      apiCalls.loadOldMessages = jest
        .fn()
        .mockResolvedValue(mockSuccessGetMessagesLastOfMultiPage);
      const { findByText } = setup();
      const loadMore = await findByText("View More Posts");
      fireEvent.click(loadMore);
      await waitFor(() => {
        expect(loadMore).not.toBeInTheDocument();
      });
    });

    it("calls loadNewMessages with messageId when clicking New Messages Count Card", async () => {
      useFakeIntervals();
      apiCalls.loadMessages = jest
        .fn()
        .mockResolvedValue(mockSuccessGetMessagesFirstOfMultiPage);
      apiCalls.loadNewMessagesCount = jest
        .fn()
        .mockResolvedValue({ data: { count: 1 } });
      apiCalls.loadNewMessages = jest
        .fn()
        .mockResolvedValue(mockSuccessGetNewMessagesList);
      const { findByText } = setup();
      await findByText("This is the latest message");
      runTimer();
      const newMessagesCount = await findByText("There is 1 new message");
      fireEvent.click(newMessagesCount);
      const firstParam = apiCalls.loadNewMessages.mock.calls[0][0];
      expect(firstParam).toBe(10);
      useRealIntervals();
    });

    it("calls loadNewMessages with messageId and username when clicking new messages count Card", async () => {
      useFakeIntervals();
      apiCalls.loadMessages = jest
        .fn()
        .mockResolvedValue(mockSuccessGetMessagesFirstOfMultiPage);
      apiCalls.loadNewMessagesCount = jest
        .fn()
        .mockResolvedValue({ data: { count: 1 } });
      apiCalls.loadNewMessages = jest
        .fn()
        .mockResolvedValue(mockSuccessGetNewMessagesList);
      const { findByText } = setup({ user: "user1" });
      await findByText("This is the latest message");
      runTimer();
      const newMessagesCount = await findByText("There is 1 new message");
      fireEvent.click(newMessagesCount);
      expect(apiCalls.loadNewMessages).toHaveBeenCalledWith(10, "user1");
      useRealIntervals();
    });

    it("displays loaded new message when loadNewMessages api call success", async () => {
      useFakeIntervals();
      apiCalls.loadMessages = jest
        .fn()
        .mockResolvedValue(mockSuccessGetMessagesFirstOfMultiPage);
      apiCalls.loadNewMessagesCount = jest
        .fn()
        .mockResolvedValue({ data: { count: 1 } });
      apiCalls.loadNewMessages = jest
        .fn()
        .mockResolvedValue(mockSuccessGetNewMessagesList);
      const { findByText } = setup({ user: "user1" });
      await findByText("This is the latest message");
      runTimer();
      const newMessagesCount = await findByText("There is 1 new message");
      fireEvent.click(newMessagesCount);
      const newMessage = await findByText("This is the newest message");

      expect(newMessage).toBeInTheDocument();
      useRealIntervals();
    });

    it("hides new messages count when loadNewMessages api call success", async () => {
      useFakeIntervals();
      apiCalls.loadMessages = jest
        .fn()
        .mockResolvedValue(mockSuccessGetMessagesFirstOfMultiPage);
      apiCalls.loadNewMessagesCount = jest
        .fn()
        .mockResolvedValue({ data: { count: 1 } });
      apiCalls.loadNewMessages = jest
        .fn()
        .mockResolvedValue(mockSuccessGetNewMessagesList);
      const { findByText, queryByText } = setup({ user: "user1" });
      await findByText("This is the latest message");
      runTimer();
      const newMessagesCount = await findByText("There is 1 new message");
      fireEvent.click(newMessagesCount);
      await findByText("This is the newest message");
      expect(queryByText("There is 1 new message")).not.toBeInTheDocument();
      useRealIntervals();
    });

    it("does not allow loadOldMessages to be called when there is an active api call about it", async () => {
      apiCalls.loadMessages = jest
        .fn()
        .mockResolvedValue(mockSuccessGetMessagesFirstOfMultiPage);
      apiCalls.loadOldMessages = jest
        .fn()
        .mockResolvedValue(mockSuccessGetMessagesLastOfMultiPage);
      const { findByText } = setup();
      const loadMore = await findByText("View More Posts");
      fireEvent.click(loadMore);
      fireEvent.click(loadMore);

      expect(apiCalls.loadOldMessages).toHaveBeenCalledTimes(1);
    });

    it("replaces View More Posts with spinner when there is an active api call about it", async () => {
      apiCalls.loadMessages = jest
        .fn()
        .mockResolvedValue(mockSuccessGetMessagesFirstOfMultiPage);
      apiCalls.loadOldMessages = jest.fn().mockImplementation(() => {
        return new Promise((resolve, reject) => {
          setTimeout(() => {
            resolve(mockSuccessGetMessagesLastOfMultiPage);
          }, 300);
        });
      });
      const { queryByText, findByText, queryByRole } = setup();
      const loadMore = await findByText("View More Posts");
      fireEvent.click(loadMore);
      const spinner = queryByRole("status");
      expect(spinner).toBeInTheDocument();
      expect(queryByText("View More Posts")).not.toBeInTheDocument();
    });

    it("replaces Spinner with View More Posts after active api call for loadOldMessages finishes with middle page response", async () => {
      apiCalls.loadMessages = jest
        .fn()
        .mockResolvedValue(mockSuccessGetMessagesFirstOfMultiPage);
      apiCalls.loadOldMessages = jest.fn().mockImplementation(() => {
        return new Promise((resolve, reject) => {
          setTimeout(() => {
            resolve(mockSuccessGetMessagesMiddleOfMultiPage);
          }, 300);
        });
      });
      const { queryByText, findByText, queryByRole } = setup();
      const loadMore = await findByText("View More Posts");
      fireEvent.click(loadMore);
      await findByText("This message is in middle page");
      expect(queryByRole("status")).not.toBeInTheDocument();
      expect(queryByText("View More Posts")).toBeInTheDocument();
    });

    it("replaces Spinner with View More Posts after active api call for loadOldMessages finishes error", async () => {
      apiCalls.loadMessages = jest
        .fn()
        .mockResolvedValue(mockSuccessGetMessagesFirstOfMultiPage);
      apiCalls.loadOldMessages = jest.fn().mockImplementation(() => {
        return new Promise((resolve, reject) => {
          setTimeout(() => {
            reject({ response: { data: {} } });
          }, 300);
        });
      });
      const { queryByText, findByText, queryByRole } = setup();
      const loadMore = await findByText("View More Posts");
      fireEvent.click(loadMore);
      await waitForElementToBeRemoved(() => queryByRole("status"));

      const spinner = queryByRole("status");
      expect(spinner).not.toBeInTheDocument();
      expect(queryByText("View More Posts")).toBeInTheDocument();
    });

    it("does not allow loadNewMessages to be called when there is an active api call about it", async () => {
      useFakeIntervals();
      apiCalls.loadMessages = jest
        .fn()
        .mockResolvedValue(mockSuccessGetMessagesFirstOfMultiPage);
      apiCalls.loadNewMessagesCount = jest
        .fn()
        .mockResolvedValue({ data: { count: 1 } });
      apiCalls.loadNewMessages = jest
        .fn()
        .mockResolvedValue(mockSuccessGetNewMessagesList);
      const { findByText } = setup({ user: "user1" });
      await findByText("This is the latest message");
      runTimer();
      const newMessagesCount = await findByText("There is 1 new message");

      fireEvent.click(newMessagesCount);
      fireEvent.click(newMessagesCount);

      expect(apiCalls.loadNewMessages).toHaveBeenCalledTimes(1);
      useRealIntervals();
    });

    it("replaces There is 1 new message with spinner when there is an active api call about it", async () => {
      useFakeIntervals();
      apiCalls.loadMessages = jest
        .fn()
        .mockResolvedValue(mockSuccessGetMessagesFirstOfMultiPage);
      apiCalls.loadNewMessagesCount = jest
        .fn()
        .mockResolvedValue({ data: { count: 1 } });
      apiCalls.loadNewMessages = jest.fn().mockImplementation(() => {
        return new Promise((resolve, reject) => {
          setTimeout(() => {
            resolve(mockSuccessGetNewMessagesList);
          }, 300);
        });
      });
      const { queryByText, findByText, queryByRole } = setup();
      await findByText("This is the latest message");
      runTimer();
      const newMessagesCount = await findByText("There is 1 new message");
      fireEvent.click(newMessagesCount);
      await waitForElementToBeRemoved(() => queryByRole("status"));

      const spinner = queryByRole("status");
      expect(spinner).not.toBeInTheDocument();
      expect(queryByText("There is 1 new message")).not.toBeInTheDocument();
      useRealIntervals();
    });

    it("removes Spinner and There is 1 new message after active api call for loadNewMessages finishes with success", async () => {
      useFakeIntervals();
      apiCalls.loadMessages = jest
        .fn()
        .mockResolvedValue(mockSuccessGetMessagesFirstOfMultiPage);
      apiCalls.loadNewMessagesCount = jest
        .fn()
        .mockResolvedValue({ data: { count: 1 } });
      apiCalls.loadNewMessages = jest
        .fn()
        .mockResolvedValue(mockSuccessGetNewMessagesList);
      const { queryByText, findByText } = setup({ user: "user1" });
      await findByText("This is the latest message");
      runTimer();
      const newMessagesCount = await findByText("There is 1 new message");
      fireEvent.click(newMessagesCount);
      await findByText("This is the newest message");
      expect(queryByText("Loading...")).not.toBeInTheDocument();
      expect(queryByText("There is 1 new message")).not.toBeInTheDocument();
      useRealIntervals();
    });

    it("replaces Spinner with There is 1 new message after active api call for loadNewMessages fails", async () => {
      useFakeIntervals();
      apiCalls.loadMessages = jest
        .fn()
        .mockResolvedValue(mockSuccessGetMessagesFirstOfMultiPage);
      apiCalls.loadNewMessagesCount = jest
        .fn()
        .mockResolvedValue({ data: { count: 1 } });
      apiCalls.loadNewMessages = jest.fn().mockImplementation(() => {
        return new Promise((resolve, reject) => {
          setTimeout(() => {
            reject({ response: { data: {} } });
          }, 300);
        });
      });
      const { queryByText, findByText, queryByRole } = setup();
      await findByText("This is the latest message");
      runTimer();
      const newMessagesCount = await findByText("There is 1 new message");
      fireEvent.click(newMessagesCount);
      await waitForElementToBeRemoved(() => queryByRole("status"));

      const spinner = queryByRole("status");
      await waitFor(() => {
        expect(spinner).not.toBeInTheDocument();
        expect(queryByText("There is 1 new message")).toBeInTheDocument();
      });
      useRealIntervals();
    });

    it("displays modal with information about the action", async () => {
      apiCalls.loadMessages = jest
        .fn()
        .mockResolvedValue(mockSuccessGetMessagesFirstOfMultiPage);
      apiCalls.loadNewMessagesCount = jest
        .fn()
        .mockResolvedValue({ data: { count: 1 } });
      const { container, queryByText, findByText } = setup();
      await findByText("This is the latest message");
      const deleteButton = container.querySelectorAll("button")[0];
      fireEvent.click(deleteButton);

      const message = queryByText(
        `Are you sure you want to remove this message? This cannot be undone.`
      );
      expect(message).toBeInTheDocument();
    });

    it("calls deleteMessage api with messageId when delete button is clicked on modal", async () => {
      apiCalls.loadMessages = jest
        .fn()
        .mockResolvedValue(mockSuccessGetMessagesFirstOfMultiPage);
      apiCalls.loadNewMessagesCount = jest
        .fn()
        .mockResolvedValue({ data: { count: 1 } });

      apiCalls.deleteMessage = jest.fn().mockResolvedValue({});
      const { container, queryByText, findByText } = setup();
      await findByText("This is the latest message");
      const deleteButton = container.querySelectorAll("button")[0];
      fireEvent.click(deleteButton);
      const deleteMessageButton = queryByText("Delete Post");
      fireEvent.click(deleteMessageButton);
      expect(apiCalls.deleteMessage).toHaveBeenCalledWith(10);
    });

    it("removes the deleted message from document after successful deleteMessage api call", async () => {
      apiCalls.loadMessages = jest
        .fn()
        .mockResolvedValue(mockSuccessGetMessagesFirstOfMultiPage);
      apiCalls.loadNewMessagesCount = jest
        .fn()
        .mockResolvedValue({ data: { count: 1 } });

      apiCalls.deleteMessage = jest.fn().mockResolvedValue({});
      const { container, queryByText, findByText } = setup();
      await findByText("This is the latest message");
      const deleteButton = container.querySelectorAll("button")[0];
      fireEvent.click(deleteButton);
      const deleteMessageButton = queryByText("Delete Post");
      fireEvent.click(deleteMessageButton);
      await waitFor(() => {
        const deletedmessageContent = queryByText("This is the latest message");
        expect(deletedmessageContent).not.toBeInTheDocument();
      });
    });

    it("disables Modal Buttons when api call in progress", async () => {
      apiCalls.loadMessages = jest
        .fn()
        .mockResolvedValue(mockSuccessGetMessagesFirstOfMultiPage);
      apiCalls.loadNewMessagesCount = jest
        .fn()
        .mockResolvedValue({ data: { count: 1 } });

      apiCalls.deleteMessage = jest.fn().mockImplementation(() => {
        return new Promise((resolve, reject) => {
          setTimeout(() => {
            resolve({});
          }, 300);
        });
      });
      const { container, queryByText, findByText } = setup();
      await findByText("This is the latest message");
      const deleteButton = container.querySelectorAll("button")[0];
      fireEvent.click(deleteButton);
      const deleteMessageButton = queryByText("Delete Post");
      fireEvent.click(deleteMessageButton);

      expect(deleteMessageButton).toBeDisabled();
      expect(queryByText("Cancel")).toBeDisabled();
    });

    it("displays spinner when api call in progress", async () => {
      apiCalls.loadMessages = jest
        .fn()
        .mockResolvedValue(mockSuccessGetMessagesFirstOfMultiPage);
      apiCalls.loadNewMessagesCount = jest
        .fn()
        .mockResolvedValue({ data: { count: 1 } });

      apiCalls.deleteMessage = jest.fn().mockImplementation(() => {
        return new Promise((resolve, reject) => {
          setTimeout(() => {
            resolve({});
          }, 300);
        });
      });
      const { container, queryByText, findByText, queryByRole } = setup();
      await findByText("This is the latest message");
      const deleteButton = container.querySelectorAll("button")[0];
      fireEvent.click(deleteButton);
      const deleteMessageButton = queryByText("Delete Post");
      fireEvent.click(deleteMessageButton);

      await waitForElementToBeRemoved(() => queryByRole("status"));
      const spinner = queryByRole("status");
      expect(spinner).not.toBeInTheDocument();
    });

    it("hides spinner when api call finishes", async () => {
      apiCalls.loadMessages = jest
        .fn()
        .mockResolvedValue(mockSuccessGetMessagesFirstOfMultiPage);
      apiCalls.loadNewMessagesCount = jest
        .fn()
        .mockResolvedValue({ data: { count: 1 } });

      apiCalls.deleteMessage = jest.fn().mockImplementation(() => {
        return new Promise((resolve, reject) => {
          setTimeout(() => {
            resolve({});
          }, 300);
        });
      });
      const { container, queryByText, findByText, queryByRole } = setup();
      await findByText("This is the latest message");
      const deleteButton = container.querySelectorAll("button")[0];
      fireEvent.click(deleteButton);
      const deleteMessageButton = queryByText("Delete Post");
      fireEvent.click(deleteMessageButton);
      await waitForElementToBeRemoved(() => queryByRole("status"));

      await waitFor(() => {
        const spinner = queryByRole("status");
        expect(spinner).not.toBeInTheDocument();
      });
    });



    //////////

    it('calls the messageReaction when clicked the like button', async () => {
      apiCalls.loadMessages = jest.fn().mockResolvedValueOnce(mockResponseWithLoadedMessagePage());
      apiCalls.messageReaction = jest.fn().mockResolvedValueOnce({});
      const { queryByTestId, queryByText } = setup();
      await waitFor(() => queryByText('This is the loaded message'));

      const like = queryByTestId('like-reaction');
      fireEvent.click(like);
      expect(apiCalls.messageReaction).toBeCalledWith(15, 'like');
    });

    it('updates the loggedUserReaction to like and count after the successfull messageReaction when clicked the like button', async () => {
      apiCalls.loadMessages = jest.fn().mockResolvedValueOnce(mockResponseWithLoadedMessagePage());
      apiCalls.messageReaction = jest.fn().mockResolvedValueOnce({});
      const { queryByTestId, queryByText } = setup();
      await waitFor(() => queryByText('This is the loaded message'));

      const like = queryByTestId('like-reaction');
      fireEvent.click(like);

      await waitForDomChange();
      const likeAfterClick = queryByTestId('like-reaction');
      expect(likeAfterClick.className).toContain('text-success');
      expect(likeAfterClick.textContent).toBe("6");
    });

    it('updates the loggedUserReaction from like to null and count after the successfull messageReaction when clicked the like button', async () => {
      const mockData = mockResponseWithLoadedMessagePage()
      mockData.data.content[0].reactions.loggedUserReaction = 'LIKE';

      apiCalls.loadMessages = jest.fn().mockResolvedValueOnce(mockData);
      apiCalls.messageReaction = jest.fn().mockResolvedValueOnce({});
      const { queryByTestId, queryByText } = setup();
      await waitFor(() => queryByText('This is the loaded message'));

      const like = queryByTestId('like-reaction');
      fireEvent.click(like);

      await waitForDomChange();
      const likeAfterClick = queryByTestId('like-reaction');
      expect(likeAfterClick.className).not.toContain('text-success');
      expect(likeAfterClick.textContent).toBe("4");
    });

    it('updates the loggedUserReaction to dislike and count after the successfull messageReaction when clicked the dislike button', async () => {
      apiCalls.loadMessages = jest.fn().mockResolvedValueOnce(mockResponseWithLoadedMessagePage());
      apiCalls.messageReaction = jest.fn().mockResolvedValueOnce({});
      const { queryByTestId, queryByText } = setup();
      await waitFor(() => queryByText('This is the loaded message'));

      const dislike = queryByTestId('dislike-reaction');
      fireEvent.click(dislike);

      await waitForDomChange();
      const dislikeAfterClick = queryByTestId('dislike-reaction');
      expect(dislikeAfterClick.className).toContain('text-danger');
      expect(dislikeAfterClick.textContent).toBe("8");
    });

    it('updates the loggedUserReaction from dislike to null and count after the successfull messageReaction when clicked the dislike button', async () => {
      const mockData = mockResponseWithLoadedMessagePage()
      mockData.data.content[0].reactions.loggedUserReaction = 'DISLIKE';

      apiCalls.loadMessages = jest.fn().mockResolvedValueOnce(mockData);
      apiCalls.messageReaction = jest.fn().mockResolvedValueOnce({});
      const { queryByTestId, queryByText } = setup();
      await waitFor(() => queryByText('This is the loaded message'));

      const dislike = queryByTestId('dislike-reaction');
      fireEvent.click(dislike);

      await waitForDomChange();
      const dislikeAfterClick = queryByTestId('dislike-reaction');
      expect(dislikeAfterClick.className).not.toContain('text-danger');
      expect(dislikeAfterClick.textContent).toBe("6");
    });

    it('updates the loggedUserReaction from dislike to like and count after the successfull messageReaction when clicked the like button', async () => {
      const mockData = mockResponseWithLoadedMessagePage()
      mockData.data.content[0].reactions.loggedUserReaction = 'DISLIKE';

      apiCalls.loadMessages = jest.fn().mockResolvedValueOnce(mockData);
      apiCalls.messageReaction = jest.fn().mockResolvedValueOnce({});
      const { queryByTestId, queryByText } = setup();
      await waitFor(() => queryByText('This is the loaded message'));

      const like = queryByTestId('like-reaction');
      fireEvent.click(like);

      await waitForDomChange();
      const likeAfterClick = queryByTestId('like-reaction');
      expect(likeAfterClick.className).toContain('text-success');
      expect(likeAfterClick.textContent).toBe("6");

      const dislikeAfterClick = queryByTestId('dislike-reaction');
      expect(dislikeAfterClick.className).not.toContain('text-danger');
      expect(dislikeAfterClick.textContent).toBe("6");
    });

    it('updates the loggedUserReaction from like to dislike and count after the successfull messageReaction when clicked the dislike button', async () => {
      const mockData = mockResponseWithLoadedMessagePage()
      mockData.data.content[0].reactions.loggedUserReaction = 'LIKE';

      apiCalls.loadMessages = jest.fn().mockResolvedValueOnce(mockData);
      apiCalls.messageReaction = jest.fn().mockResolvedValueOnce({});
      const { queryByTestId, queryByText } = setup();
      await waitFor(() => queryByText('This is the loaded message'));

      const dislike = queryByTestId('dislike-reaction');
      fireEvent.click(dislike);

      await waitForDomChange();
      const likeAfterClick = queryByTestId('like-reaction');
      expect(likeAfterClick.className).not.toContain('text-success');
      expect(likeAfterClick.textContent).toBe("4");

      const dislikeAfterClick = queryByTestId('dislike-reaction');
      expect(dislikeAfterClick.className).toContain('text-danger');
      expect(dislikeAfterClick.textContent).toBe("8");
    });
  });
});

console.error = () => { };

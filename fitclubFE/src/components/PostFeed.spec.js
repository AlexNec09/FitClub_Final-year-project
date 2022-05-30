import React from "react";
import {
  render,
  fireEvent,
  waitFor,
  waitForElementToBeRemoved,
} from "@testing-library/react";
import PostFeed from "./PostFeed";
import * as apiCalls from "../api/apiCalls";
import { MemoryRouter } from "react-router-dom";
import { Provider } from "react-redux";
import { createStore } from "redux";
import authReducer from "../redux/authReducer";
import createJWKSMock from "mock-jwks";


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
        <PostFeed {...props} />
      </MemoryRouter>
    </Provider>
  );
};

const mockEmptyResponse = {
  data: {
    content: [],
  },
};

const mockResponseWithLoadedPostPage = () => {
  return {
    data: {
      content: [
        {
          id: 15,
          content: 'This is the loaded post',
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

const mockSuccessGetNewPostsList = {
  data: [
    {
      id: 21,
      content: "This is the newest post",
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

const mockSuccessgetPostsMiddleOfMultiPage = {
  data: {
    content: [
      {
        id: 5,
        content: "This post is in middle page",
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

const mockSuccessgetPostsinglePage = {
  data: {
    content: [
      {
        id: 10,
        content: "This is the latest post",
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

const mockSuccessgetPostsFirstOfMultiPage = {
  data: {
    content: [
      {
        id: 10,
        content: "This is the latest post",
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
        content: "This is post 9",
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

const mockSuccessgetPostsLastOfMultiPage = {
  data: {
    content: [
      {
        id: 1,
        content: "This is the oldest post",
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
describe("PostFeed", () => {
  const jwks = createJWKSMock("http://localhost:3000/");

  beforeEach(() => {
    jwks.start();
  });

  afterEach(() => {
    jwks.stop();
  });

  describe("Lifecycle", () => {
    it("calls loadPosts when it is rendered", () => {
      apiCalls.loadPosts = jest.fn().mockResolvedValue(mockEmptyResponse);
      setup();
      expect(apiCalls.loadPosts).toHaveBeenCalled();
    });

    it("calls loadPosts with user parameter when it is rendered with user property", () => {
      const token = jwks.token({});
      const stateWithMockJwt = Object.assign(loggedInStateUser1, { jwt: token });

      apiCalls.loadPosts = jest.fn().mockResolvedValue(mockEmptyResponse);
      setup({ user: 'user1' }, stateWithMockJwt);
      expect(apiCalls.loadPosts).toHaveBeenCalledWith("user1", token);
    });

    it("calls loadPosts without user parameter when it is rendered without user property", () => {
      apiCalls.loadPosts = jest.fn().mockResolvedValue(mockEmptyResponse);
      setup();
      const parameter = apiCalls.loadPosts.mock.calls[0][0];
      expect(parameter).toBeUndefined();
    });

    it("calls loadNewPostsCount with topPostId", async () => {
      jest.useFakeTimers();
      apiCalls.loadPosts = jest
        .fn()
        .mockResolvedValue(mockSuccessgetPostsFirstOfMultiPage);
      apiCalls.loadNewPostsCount = jest
        .fn()
        .mockResolvedValue({ data: { count: 1 } });
      const { findByText } = setup();
      await findByText("This is the latest post");
      jest.runOnlyPendingTimers();
      await findByText("There is 1 new post");
      const firstParam = apiCalls.loadNewPostsCount.mock.calls[0][0];
      expect(firstParam).toBe(10);
      jest.useRealTimers();
    });

    it("calls loadNewPostsCount with topPostId and username when rendered with user property", async () => {
      jest.useFakeTimers();
      const token = jwks.token({});
      const stateWithMockJwt = Object.assign(loggedInStateUser1, { jwt: token });

      apiCalls.loadPosts = jest
        .fn()
        .mockResolvedValue(mockSuccessgetPostsFirstOfMultiPage);
      apiCalls.loadNewPostsCount = jest
        .fn()
        .mockResolvedValue({ data: { count: 1 } });
      const { findByText } = setup({ user: "user1" }, stateWithMockJwt);
      await findByText("This is the latest post");
      jest.runOnlyPendingTimers();
      await findByText("There is 1 new post");
      expect(apiCalls.loadNewPostsCount).toBeCalledWith(10, "user1", token);
      jest.useRealTimers();
    });

    it("displays new posts count as 1 after loadNewPostsCount success", async () => {
      useFakeIntervals();
      apiCalls.loadPosts = jest
        .fn()
        .mockResolvedValue(mockSuccessgetPostsFirstOfMultiPage);
      apiCalls.loadNewPostsCount = jest
        .fn()
        .mockResolvedValue({ data: { count: 1 } });
      const { findByText } = setup({ user: "user1" });
      await findByText("This is the latest post");
      runTimer();
      const newPostsCount = await findByText("There is 1 new post");
      expect(newPostsCount).toBeInTheDocument();
      useRealIntervals();
    });

    it("displays new posts count constantly", async () => {
      useFakeIntervals();
      apiCalls.loadPosts = jest
        .fn()
        .mockResolvedValue(mockSuccessgetPostsFirstOfMultiPage);
      apiCalls.loadNewPostsCount = jest
        .fn()
        .mockResolvedValue({ data: { count: 1 } });
      const { findByText } = setup({ user: "user1" });
      await findByText("This is the latest post");
      runTimer();
      await findByText("There is 1 new post");
      apiCalls.loadNewPostsCount = jest
        .fn()
        .mockResolvedValue({ data: { count: 2 } });
      runTimer();
      const newPostsCount = await findByText("There are 2 new posts");
      expect(newPostsCount).toBeInTheDocument();
      useRealIntervals();
    });

    it("does not call loadNewPostsCount after component is unmounted", async () => {
      useFakeIntervals();
      apiCalls.loadPosts = jest
        .fn()
        .mockResolvedValue(mockSuccessgetPostsFirstOfMultiPage);
      apiCalls.loadNewPostsCount = jest
        .fn()
        .mockResolvedValue({ data: { count: 1 } });
      const { findByText, unmount } = setup({ user: "user1" });
      await findByText("This is the latest post");
      runTimer();
      await findByText("There is 1 new post");
      unmount();
      expect(apiCalls.loadNewPostsCount).toHaveBeenCalledTimes(1);
      useRealIntervals();
    });

    it("displays new posts count as 1 after loadNewPostsCount success when user does not have posts initially", async () => {
      useFakeIntervals();
      apiCalls.loadPosts = jest.fn().mockResolvedValue(mockEmptyResponse);
      apiCalls.loadNewPostsCount = jest
        .fn()
        .mockResolvedValue({ data: { count: 1 } });
      const { findByText } = setup({ user: "user1" });
      await findByText("There are no posts");
      runTimer();
      const newPostsCount = await findByText("There is 1 new post");
      expect(newPostsCount).toBeInTheDocument();
      useRealIntervals();
    });
  });

  describe("Layout", () => {
    it("displays no post when the response has empty page", async () => {
      apiCalls.loadPosts = jest.fn().mockResolvedValue(mockEmptyResponse);
      const { findByText } = setup();
      const post = await findByText("There are no posts");
      expect(post).toBeInTheDocument();
    });

    it("does not display no post when the response has page of post", async () => {
      apiCalls.loadPosts = jest
        .fn()
        .mockResolvedValue(mockSuccessgetPostsinglePage);
      const { queryByText } = setup();
      const post = queryByText("There are no posts");
      await waitFor(() => {
        expect(post).not.toBeInTheDocument();
      });
    });

    it("displays spinner when loading the posts", async () => {
      apiCalls.loadPosts = jest.fn().mockImplementation(() => {
        return new Promise((resolve, reject) => {
          setTimeout(() => {
            resolve(mockSuccessgetPostsinglePage);
          }, 300);
        });
      });
      const { queryByRole } = setup();
      const spinner = queryByRole("status");
      expect(spinner).toBeInTheDocument();
    });

    it("displays post content", async () => {
      apiCalls.loadPosts = jest
        .fn()
        .mockResolvedValue(mockSuccessgetPostsinglePage);
      const { findByText } = setup();
      const postContent = await findByText("This is the latest post");
      expect(postContent).toBeInTheDocument();
    });

    it("displays View More Posts when there are next pages", async () => {
      apiCalls.loadPosts = jest
        .fn()
        .mockResolvedValue(mockSuccessgetPostsFirstOfMultiPage);
      const { findByText } = setup();
      const loadMore = await findByText("View More Posts");
      expect(loadMore).toBeInTheDocument();
    });
  });

  describe("Interactions", () => {
    it("calls loadOldPosts with postId when clicking View More Posts", async () => {
      apiCalls.loadPosts = jest
        .fn()
        .mockResolvedValue(mockSuccessgetPostsFirstOfMultiPage);
      apiCalls.loadOldPosts = jest
        .fn()
        .mockResolvedValue(mockSuccessgetPostsLastOfMultiPage);
      const { findByText } = setup();
      const loadMore = await findByText("View More Posts");
      fireEvent.click(loadMore);
      const firstParam = apiCalls.loadOldPosts.mock.calls[0][0];
      expect(firstParam).toBe(9);
    });

    it("calls loadOldPosts with postId and username when clicking View More Posts when rendered with user property", async () => {
      const token = jwks.token({});
      const stateWithMockJwt = Object.assign(loggedInStateUser1, { jwt: token });

      apiCalls.loadPosts = jest
        .fn()
        .mockResolvedValue(mockSuccessgetPostsFirstOfMultiPage);
      apiCalls.loadOldPosts = jest
        .fn()
        .mockResolvedValue(mockSuccessgetPostsLastOfMultiPage);
      const { findByText } = setup({ user: "user1" }, stateWithMockJwt);
      const loadMore = await findByText("View More Posts");
      fireEvent.click(loadMore);
      expect(apiCalls.loadOldPosts).toHaveBeenCalledWith(9, "user1", token);
    });

    it("displays loaded old post when loadOldPosts api call success", async () => {
      apiCalls.loadPosts = jest
        .fn()
        .mockResolvedValue(mockSuccessgetPostsFirstOfMultiPage);
      apiCalls.loadOldPosts = jest
        .fn()
        .mockResolvedValue(mockSuccessgetPostsLastOfMultiPage);
      const { findByText } = setup();
      const loadMore = await findByText("View More Posts");
      fireEvent.click(loadMore);
      const oldPost = await findByText("This is the oldest post");
      expect(oldPost).toBeInTheDocument();
    });

    it("hides View More Posts when loadOldPosts api call returns last page", async () => {
      apiCalls.loadPosts = jest
        .fn()
        .mockResolvedValue(mockSuccessgetPostsFirstOfMultiPage);
      apiCalls.loadOldPosts = jest
        .fn()
        .mockResolvedValue(mockSuccessgetPostsLastOfMultiPage);
      const { findByText } = setup();
      const loadMore = await findByText("View More Posts");
      fireEvent.click(loadMore);
      await waitFor(() => {
        expect(loadMore).not.toBeInTheDocument();
      });
    });

    it("calls loadNewPosts with postId when clicking New Posts Count Card", async () => {
      useFakeIntervals();
      apiCalls.loadPosts = jest
        .fn()
        .mockResolvedValue(mockSuccessgetPostsFirstOfMultiPage);
      apiCalls.loadNewPostsCount = jest
        .fn()
        .mockResolvedValue({ data: { count: 1 } });
      apiCalls.loadNewPosts = jest
        .fn()
        .mockResolvedValue(mockSuccessGetNewPostsList);
      const { findByText } = setup();
      await findByText("This is the latest post");
      runTimer();
      const newPostsCount = await findByText("There is 1 new post");
      fireEvent.click(newPostsCount);
      const firstParam = apiCalls.loadNewPosts.mock.calls[0][0];
      expect(firstParam).toBe(10);
      useRealIntervals();
    });

    it("calls loadNewPosts with postId and username when clicking new posts count Card", async () => {
      useFakeIntervals();

      const token = jwks.token({});
      const stateWithMockJwt = Object.assign(loggedInStateUser1, { jwt: token });

      apiCalls.loadPosts = jest
        .fn()
        .mockResolvedValue(mockSuccessgetPostsFirstOfMultiPage);
      apiCalls.loadNewPostsCount = jest
        .fn()
        .mockResolvedValue({ data: { count: 1 } });
      apiCalls.loadNewPosts = jest
        .fn()
        .mockResolvedValue(mockSuccessGetNewPostsList);
      const { findByText } = setup({ user: "user1" }, stateWithMockJwt);
      await findByText("This is the latest post");
      runTimer();
      const newPostsCount = await findByText("There is 1 new post");
      fireEvent.click(newPostsCount);
      expect(apiCalls.loadNewPosts).toHaveBeenCalledWith(10, "user1", token);
      useRealIntervals();
    });

    it("displays loaded new post when loadNewPosts api call success", async () => {
      useFakeIntervals();
      apiCalls.loadPosts = jest
        .fn()
        .mockResolvedValue(mockSuccessgetPostsFirstOfMultiPage);
      apiCalls.loadNewPostsCount = jest
        .fn()
        .mockResolvedValue({ data: { count: 1 } });
      apiCalls.loadNewPosts = jest
        .fn()
        .mockResolvedValue(mockSuccessGetNewPostsList);
      const { findByText } = setup({ user: "user1" });
      await findByText("This is the latest post");
      runTimer();
      const newPostsCount = await findByText("There is 1 new post");
      fireEvent.click(newPostsCount);
      const newPost = await findByText("This is the newest post");

      expect(newPost).toBeInTheDocument();
      useRealIntervals();
    });

    it("hides new posts count when loadNewPosts api call success", async () => {
      useFakeIntervals();
      apiCalls.loadPosts = jest
        .fn()
        .mockResolvedValue(mockSuccessgetPostsFirstOfMultiPage);
      apiCalls.loadNewPostsCount = jest
        .fn()
        .mockResolvedValue({ data: { count: 1 } });
      apiCalls.loadNewPosts = jest
        .fn()
        .mockResolvedValue(mockSuccessGetNewPostsList);
      const { findByText, queryByText } = setup({ user: "user1" });
      await findByText("This is the latest post");
      runTimer();
      const newPostsCount = await findByText("There is 1 new post");
      fireEvent.click(newPostsCount);
      await findByText("This is the newest post");
      expect(queryByText("There is 1 new post")).not.toBeInTheDocument();
      useRealIntervals();
    });

    it("does not allow loadOldPosts to be called when there is an active api call about it", async () => {
      apiCalls.loadPosts = jest
        .fn()
        .mockResolvedValue(mockSuccessgetPostsFirstOfMultiPage);
      apiCalls.loadOldPosts = jest
        .fn()
        .mockResolvedValue(mockSuccessgetPostsLastOfMultiPage);
      const { findByText } = setup();
      const loadMore = await findByText("View More Posts");
      fireEvent.click(loadMore);
      fireEvent.click(loadMore);

      expect(apiCalls.loadOldPosts).toHaveBeenCalledTimes(1);
    });

    it("replaces View More Posts with spinner when there is an active api call about it", async () => {
      apiCalls.loadPosts = jest
        .fn()
        .mockResolvedValue(mockSuccessgetPostsFirstOfMultiPage);
      apiCalls.loadOldPosts = jest.fn().mockImplementation(() => {
        return new Promise((resolve, reject) => {
          setTimeout(() => {
            resolve(mockSuccessgetPostsLastOfMultiPage);
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

    it("replaces Spinner with View More Posts after active api call for loadOldPosts finishes with middle page response", async () => {
      apiCalls.loadPosts = jest
        .fn()
        .mockResolvedValue(mockSuccessgetPostsFirstOfMultiPage);
      apiCalls.loadOldPosts = jest.fn().mockImplementation(() => {
        return new Promise((resolve, reject) => {
          setTimeout(() => {
            resolve(mockSuccessgetPostsMiddleOfMultiPage);
          }, 300);
        });
      });
      const { queryByText, findByText, queryByRole } = setup();
      const loadMore = await findByText("View More Posts");
      fireEvent.click(loadMore);
      await findByText("This post is in middle page");
      expect(queryByRole("status")).not.toBeInTheDocument();
      expect(queryByText("View More Posts")).toBeInTheDocument();
    });

    it("replaces Spinner with View More Posts after active api call for loadOldPosts finishes error", async () => {
      apiCalls.loadPosts = jest
        .fn()
        .mockResolvedValue(mockSuccessgetPostsFirstOfMultiPage);
      apiCalls.loadOldPosts = jest.fn().mockImplementation(() => {
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

    it("does not allow loadNewPosts to be called when there is an active api call about it", async () => {
      useFakeIntervals();
      apiCalls.loadPosts = jest
        .fn()
        .mockResolvedValue(mockSuccessgetPostsFirstOfMultiPage);
      apiCalls.loadNewPostsCount = jest
        .fn()
        .mockResolvedValue({ data: { count: 1 } });
      apiCalls.loadNewPosts = jest
        .fn()
        .mockResolvedValue(mockSuccessGetNewPostsList);
      const { findByText } = setup({ user: "user1" });
      await findByText("This is the latest post");
      runTimer();
      const newPostsCount = await findByText("There is 1 new post");

      fireEvent.click(newPostsCount);
      fireEvent.click(newPostsCount);

      expect(apiCalls.loadNewPosts).toHaveBeenCalledTimes(1);
      useRealIntervals();
    });

    it("replaces There is 1 new post with spinner when there is an active api call about it", async () => {
      useFakeIntervals();
      apiCalls.loadPosts = jest
        .fn()
        .mockResolvedValue(mockSuccessgetPostsFirstOfMultiPage);
      apiCalls.loadNewPostsCount = jest
        .fn()
        .mockResolvedValue({ data: { count: 1 } });
      apiCalls.loadNewPosts = jest.fn().mockImplementation(() => {
        return new Promise((resolve, reject) => {
          setTimeout(() => {
            resolve(mockSuccessGetNewPostsList);
          }, 300);
        });
      });
      const { queryByText, findByText, queryByRole } = setup();
      await findByText("This is the latest post");
      runTimer();
      const newPostsCount = await findByText("There is 1 new post");
      fireEvent.click(newPostsCount);
      await waitForElementToBeRemoved(() => queryByRole("status"));

      const spinner = queryByRole("status");
      expect(spinner).not.toBeInTheDocument();
      expect(queryByText("There is 1 new post")).not.toBeInTheDocument();
      useRealIntervals();
    });

    it("removes Spinner and There is 1 new post after active api call for loadNewPosts finishes with success", async () => {
      useFakeIntervals();
      apiCalls.loadPosts = jest
        .fn()
        .mockResolvedValue(mockSuccessgetPostsFirstOfMultiPage);
      apiCalls.loadNewPostsCount = jest
        .fn()
        .mockResolvedValue({ data: { count: 1 } });
      apiCalls.loadNewPosts = jest
        .fn()
        .mockResolvedValue(mockSuccessGetNewPostsList);
      const { queryByText, findByText } = setup({ user: "user1" });
      await findByText("This is the latest post");
      runTimer();
      const newPostsCount = await findByText("There is 1 new post");
      fireEvent.click(newPostsCount);
      await findByText("This is the newest post");
      expect(queryByText("Loading...")).not.toBeInTheDocument();
      expect(queryByText("There is 1 new post")).not.toBeInTheDocument();
      useRealIntervals();
    });

    it("replaces Spinner with There is 1 new post after active api call for loadNewPosts fails", async () => {
      useFakeIntervals();
      apiCalls.loadPosts = jest
        .fn()
        .mockResolvedValue(mockSuccessgetPostsFirstOfMultiPage);
      apiCalls.loadNewPostsCount = jest
        .fn()
        .mockResolvedValue({ data: { count: 1 } });
      apiCalls.loadNewPosts = jest.fn().mockImplementation(() => {
        return new Promise((resolve, reject) => {
          setTimeout(() => {
            reject({ response: { data: {} } });
          }, 300);
        });
      });
      const { queryByText, findByText, queryByRole } = setup();
      await findByText("This is the latest post");
      runTimer();
      const newPostsCount = await findByText("There is 1 new post");
      fireEvent.click(newPostsCount);
      await waitForElementToBeRemoved(() => queryByRole("status"));

      const spinner = queryByRole("status");
      await waitFor(() => {
        expect(spinner).not.toBeInTheDocument();
        expect(queryByText("There is 1 new post")).toBeInTheDocument();
      });
      useRealIntervals();
    });

    it("displays modal with information about the action", async () => {
      apiCalls.loadPosts = jest
        .fn()
        .mockResolvedValue(mockSuccessgetPostsFirstOfMultiPage);
      apiCalls.loadNewPostsCount = jest
        .fn()
        .mockResolvedValue({ data: { count: 1 } });
      const { container, queryByText, findByText } = setup();
      await findByText("This is the latest post");
      const deleteButton = container.querySelectorAll("button")[0];
      fireEvent.click(deleteButton);

      const post = queryByText(
        `Are you sure you want to remove this post? This cannot be undone.`
      );
      expect(post).toBeInTheDocument();
    });

    it("calls deletePost api with postId when delete button is clicked on modal", async () => {
      const token = jwks.token({});
      const stateWithMockJwt = Object.assign(loggedInStateUser1, { jwt: token });

      apiCalls.loadPosts = jest
        .fn()
        .mockResolvedValue(mockSuccessgetPostsFirstOfMultiPage);
      apiCalls.loadNewPostsCount = jest
        .fn()
        .mockResolvedValue({ data: { count: 1 } });

      apiCalls.deletePost = jest.fn().mockResolvedValue({});
      const { container, queryByText, findByText } = setup(stateWithMockJwt);
      await findByText("This is the latest post");
      const deleteButton = container.querySelectorAll("button")[0];
      fireEvent.click(deleteButton);
      const deletePostButton = queryByText("Delete Post");
      fireEvent.click(deletePostButton);
      expect(apiCalls.deletePost).toHaveBeenCalledWith(10, token);
    });

    it("removes the deleted post from document after successful deletePost api call", async () => {
      apiCalls.loadPosts = jest
        .fn()
        .mockResolvedValue(mockSuccessgetPostsFirstOfMultiPage);
      apiCalls.loadNewPostsCount = jest
        .fn()
        .mockResolvedValue({ data: { count: 1 } });

      apiCalls.deletePost = jest.fn().mockResolvedValue({});
      const { container, queryByText, findByText } = setup();
      await findByText("This is the latest post");
      const deleteButton = container.querySelectorAll("button")[0];
      fireEvent.click(deleteButton);
      const deletePostButton = queryByText("Delete Post");
      fireEvent.click(deletePostButton);
      await waitFor(() => {
        const deletedPostContent = queryByText("This is the latest post");
        expect(deletedPostContent).not.toBeInTheDocument();
      });
    });

    it("disables Modal Buttons when api call in progress", async () => {
      apiCalls.loadPosts = jest
        .fn()
        .mockResolvedValue(mockSuccessgetPostsFirstOfMultiPage);
      apiCalls.loadNewPostsCount = jest
        .fn()
        .mockResolvedValue({ data: { count: 1 } });

      apiCalls.deletePost = jest.fn().mockImplementation(() => {
        return new Promise((resolve, reject) => {
          setTimeout(() => {
            resolve({});
          }, 300);
        });
      });
      const { container, queryByText, findByText } = setup();
      await findByText("This is the latest post");
      const deleteButton = container.querySelectorAll("button")[0];
      fireEvent.click(deleteButton);
      const deletePostButton = queryByText("Delete Post");
      fireEvent.click(deletePostButton);

      expect(deletePostButton).toBeDisabled();
      expect(queryByText("Cancel")).toBeDisabled();
    });

    it("displays spinner when api call in progress", async () => {
      apiCalls.loadPosts = jest
        .fn()
        .mockResolvedValue(mockSuccessgetPostsFirstOfMultiPage);
      apiCalls.loadNewPostsCount = jest
        .fn()
        .mockResolvedValue({ data: { count: 1 } });

      apiCalls.deletePost = jest.fn().mockImplementation(() => {
        return new Promise((resolve, reject) => {
          setTimeout(() => {
            resolve({});
          }, 300);
        });
      });
      const { container, queryByText, findByText, queryByRole } = setup();
      await findByText("This is the latest post");
      const deleteButton = container.querySelectorAll("button")[0];
      fireEvent.click(deleteButton);
      const deletePostButton = queryByText("Delete Post");
      fireEvent.click(deletePostButton);

      await waitForElementToBeRemoved(() => queryByRole("status"));
      const spinner = queryByRole("status");
      expect(spinner).not.toBeInTheDocument();
    });

    it("hides spinner when api call finishes", async () => {
      apiCalls.loadPosts = jest
        .fn()
        .mockResolvedValue(mockSuccessgetPostsFirstOfMultiPage);
      apiCalls.loadNewPostsCount = jest
        .fn()
        .mockResolvedValue({ data: { count: 1 } });

      apiCalls.deletePost = jest.fn().mockImplementation(() => {
        return new Promise((resolve, reject) => {
          setTimeout(() => {
            resolve({});
          }, 300);
        });
      });
      const { container, queryByText, findByText, queryByRole } = setup();
      await findByText("This is the latest post");
      const deleteButton = container.querySelectorAll("button")[0];
      fireEvent.click(deleteButton);
      const deletePostButton = queryByText("Delete Post");
      fireEvent.click(deletePostButton);
      await waitForElementToBeRemoved(() => queryByRole("status"));

      await waitFor(() => {
        const spinner = queryByRole("status");
        expect(spinner).not.toBeInTheDocument();
      });
    });

    it('calls the postReaction when clicked the like button', async () => {
      const token = jwks.token({});
      const stateWithMockJwt = Object.assign(loggedInStateUser1, { jwt: token });

      apiCalls.loadPosts = jest.fn().mockResolvedValueOnce(mockResponseWithLoadedPostPage());
      apiCalls.postReaction = jest.fn().mockResolvedValueOnce({});
      const { queryByTestId, queryByText } = setup(stateWithMockJwt);
      await waitFor(() => queryByText('This is the loaded post'));

      const like = queryByTestId('like-reaction');
      fireEvent.click(like);
      expect(apiCalls.postReaction).toBeCalledWith(15, 'like', token);
    });

    it('updates the loggedUserReaction to like and count after the successfull postReaction when clicked the like button', async () => {
      apiCalls.loadPosts = jest.fn().mockResolvedValueOnce(mockResponseWithLoadedPostPage());
      apiCalls.postReaction = jest.fn().mockResolvedValueOnce({});
      const { queryByTestId, queryByText } = setup();
      await waitFor(() => queryByText('This is the loaded post'));

      const like = queryByTestId('like-reaction');
      fireEvent.click(like);

      let likeAfterClick;

      await waitFor(() => {
        likeAfterClick = queryByTestId('like-reaction');
        expect(likeAfterClick.className).toContain('text-success');
      });

      expect(likeAfterClick.textContent).toBe("6");
    });

    it('updates the loggedUserReaction from like to null and count after the successfull postReaction when clicked the like button', async () => {
      const mockData = mockResponseWithLoadedPostPage()
      mockData.data.content[0].reactions.loggedUserReaction = 'LIKE';

      apiCalls.loadPosts = jest.fn().mockResolvedValueOnce(mockData);
      apiCalls.postReaction = jest.fn().mockResolvedValueOnce({});
      const { queryByTestId, queryByText } = setup();
      await waitFor(() => queryByText('This is the loaded post'));

      const like = queryByTestId('like-reaction');
      fireEvent.click(like);

      let likeAfterClick;

      await waitFor(() => {
        likeAfterClick = queryByTestId('like-reaction');
        expect(likeAfterClick.className).toContain('text-success');
      });
      expect(likeAfterClick.textContent).toBe("4");
    });

    it('updates the loggedUserReaction to dislike and count after the successfull postReaction when clicked the dislike button', async () => {
      apiCalls.loadPosts = jest.fn().mockResolvedValueOnce(mockResponseWithLoadedPostPage());
      apiCalls.postReaction = jest.fn().mockResolvedValueOnce({});
      const { queryByTestId, queryByText } = setup();
      await waitFor(() => queryByText('This is the loaded post'));

      const dislike = queryByTestId('dislike-reaction');
      fireEvent.click(dislike);

      let dislikeAfterClick;

      await waitFor(() => {
        dislikeAfterClick = queryByTestId('dislike-reaction');
        expect(dislikeAfterClick.className).toContain('text-danger');
      });

      expect(dislikeAfterClick.textContent).toBe("8");
    });

    it('updates the loggedUserReaction from dislike to null and count after the successfull postReaction when clicked the dislike button', async () => {
      const mockData = mockResponseWithLoadedPostPage()
      mockData.data.content[0].reactions.loggedUserReaction = 'DISLIKE';

      apiCalls.loadPosts = jest.fn().mockResolvedValueOnce(mockData);
      apiCalls.postReaction = jest.fn().mockResolvedValueOnce({});
      const { queryByTestId, queryByText } = setup();
      await waitFor(() => queryByText('This is the loaded post'));

      const dislike = queryByTestId('dislike-reaction');
      fireEvent.click(dislike);

      let dislikeAfterClick;

      await waitFor(() => {
        dislikeAfterClick = queryByTestId('dislike-reaction');
        expect(dislikeAfterClick.className).toContain('text-danger');
      });

      expect(dislikeAfterClick.textContent).toBe("6");
    });

    it('updates the loggedUserReaction from dislike to like and count after the successfull postReaction when clicked the like button', async () => {
      const mockData = mockResponseWithLoadedPostPage()
      mockData.data.content[0].reactions.loggedUserReaction = 'DISLIKE';

      apiCalls.loadPosts = jest.fn().mockResolvedValueOnce(mockData);
      apiCalls.postReaction = jest.fn().mockResolvedValueOnce({});
      const { queryByTestId, queryByText } = setup();
      await waitFor(() => queryByText('This is the loaded post'));

      const like = queryByTestId('like-reaction');
      fireEvent.click(like);

      let likeAfterClick;

      await waitFor(() => {
        likeAfterClick = queryByTestId('like-reaction');
        expect(likeAfterClick.className).toContain('text-success');
      });

      expect(likeAfterClick.textContent).toBe("6");

      const dislikeAfterClick = queryByTestId('dislike-reaction');
      expect(dislikeAfterClick.className).not.toContain('text-danger');
      expect(dislikeAfterClick.textContent).toBe("6");
    });

    it('updates the loggedUserReaction from like to dislike and count after the successfull postReaction when clicked the dislike button', async () => {
      const mockData = mockResponseWithLoadedPostPage()
      mockData.data.content[0].reactions.loggedUserReaction = 'LIKE';

      apiCalls.loadPosts = jest.fn().mockResolvedValueOnce(mockData);
      apiCalls.postReaction = jest.fn().mockResolvedValueOnce({});
      const { queryByTestId, queryByText } = setup();
      await waitFor(() => queryByText('This is the loaded post'));

      const dislike = queryByTestId('dislike-reaction');
      fireEvent.click(dislike);

      let likeAfterClick;

      await waitFor(() => {
        likeAfterClick = queryByTestId('like-reaction');
        expect(likeAfterClick.className).toContain('text-success');
      });

      expect(likeAfterClick.textContent).toBe("4");

      const dislikeAfterClick = queryByTestId('dislike-reaction');
      expect(dislikeAfterClick.className).toContain('text-danger');
      expect(dislikeAfterClick.textContent).toBe("8");
    });
  });
});

console.error = () => { };

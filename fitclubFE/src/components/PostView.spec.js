import React from "react";
import { render, fireEvent } from "@testing-library/react";
import PostView from "./PostView";
import { MemoryRouter } from "react-router-dom";
import { Provider } from "react-redux";
import { createStore } from "redux";
import authReducer from "../redux/authReducer";

const loggedInStateUser1 = {
  id: 1,
  username: "user1",
  displayName: "display1",
  image: "image1.png",
  password: "P4ssword",
  isLoggedIn: true,
};

const loggedInStateUser2 = {
  id: 2,
  username: "user2",
  displayName: "display2",
  image: "image2.png",
  password: "P4ssword",
  isLoggedIn: true,
};

const postWithoutAttachment = {
  id: 8,
  content: "This is the first post",
  user: {
    id: 1,
    username: "user1",
    displayName: "display1",
    image: "image1.png",
  },
};

const getPostWithReactions = () => {
  const oneMinute = 60 * 1000;
  const date = new Date(new Date().getTime() - oneMinute);

  return {
    id: 15,
    content: 'Post content',
    date: date.getTime(),
    user: {
      username: 'user5',
      displayName: 'display5',
      image: ''
    },
    attachments: [
      {
        name: 'attached-image.png',
        type: 'file'
      }
    ],
    reactions: {
      likeCount: 5,
      dislikeCount: 7,
      myReaction: null
    }
  }
}

const postWithAttachment = {
  id: 8,
  content: "This is the first post",
  user: {
    id: 1,
    username: "user1",
    displayName: "display1",
    image: "image1.png",
  },
  attachment: {
    fileType: "image/png",
    name: "attached-image.png",
  },
};

const postWithPDFttachment = {
  id: 8,
  content: "This is the first post",
  user: {
    id: 1,
    username: "user1",
    displayName: "display1",
    image: "image1.png",
  },
  attachment: {
    fileType: "application/pdf",
    name: "attachment.pdf",
  },
};

const setup = (
  post = postWithoutAttachment,
  state = loggedInStateUser1
) => {
  const oneMinute = 60 * 1000;
  const date = new Date() - oneMinute;
  post.date = date;
  const store = createStore(authReducer, state);
  return render(
    <Provider store={store}>
      <MemoryRouter>
        <PostView post={post} />
      </MemoryRouter>
    </Provider>
  );
};

describe("PostView", () => {
  describe("Layout", () => {
    it("displays post content", () => {
      const { queryByText } = setup();
      expect(queryByText("This is the first post")).toBeInTheDocument();
    });

    it("displays users image", () => {
      const { container } = setup();
      const image = container.querySelector("img");
      expect(image.src).toContain("/images/profile/image1.png");
    });

    it("displays displayName", () => {
      const { queryByText } = setup();
      expect(queryByText("display1")).toBeInTheDocument();
    });

    it("displays relative time", () => {
      const { queryByText } = setup();
      expect(queryByText("1 minute ago")).toBeInTheDocument();
    });

    it("has link to user page", () => {
      const { container } = setup();
      const anchor = container.querySelector("a");
      expect(anchor.getAttribute("href")).toBe("/users/user1");
    });

    it("displays file attachment image", () => {
      const { container } = setup(postWithAttachment);
      const images = container.querySelectorAll("img");
      expect(images.length).toBe(2);
    });

    it("does not display file attachment when attachment type is not image", () => {
      const { container } = setup(postWithPDFttachment);
      const images = container.querySelectorAll("img");
      expect(images.length).toBe(1);
    });

    it("sets the attachment path as source for file attachment image", () => {
      const { container } = setup(postWithAttachment);
      const images = container.querySelectorAll("img");
      const attachmentImage = images[1];
      expect(attachmentImage.src).toContain(
        "/images/attachments/" + postWithAttachment.attachment.name
      );
    });

    it("displays delete button when post owned by logged in user", () => {
      const { container } = setup();
      expect(container.querySelector("button")).toBeInTheDocument();
    });

    it("does not display delete button when post is not owned by logged in user", () => {
      const { container } = setup(postWithoutAttachment, loggedInStateUser2);
      expect(container.querySelector("button")).not.toBeInTheDocument();
    });

    it("does not show the dropdown menu when not clicked", () => {
      const { queryByTestId } = setup();
      const dropDownMenu = queryByTestId("post-action-dropdown");
      expect(dropDownMenu).not.toHaveClass("show");
    });

    it("shows the dropdown menu after clicking the indicator", () => {
      const { queryByTestId } = setup();
      const indicator = queryByTestId("post-actions-indicator");
      fireEvent.click(indicator);

      const dropDownMenu = queryByTestId("post-action-dropdown");
      expect(dropDownMenu).toHaveClass("show");
    });
  });
});

console.error = () => { };

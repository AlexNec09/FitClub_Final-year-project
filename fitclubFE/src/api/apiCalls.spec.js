import axios from "axios";
import * as apiCalls from "./apiCalls";
describe("apiCalls", () => {
  describe("signup", () => {
    it("calls /api/1.0/users", () => {
      const mockSignup = jest.fn();
      axios.post = mockSignup;
      apiCalls.signup();
      const path = mockSignup.mock.calls[0][0];
      expect(path).toBe("/api/1.0/users");
    });
  });

  describe("login", () => {
    it("calls /api/1.0/login", () => {
      const mockLogin = jest.fn();
      axios.post = mockLogin;
      apiCalls.login({ username: "test-user", password: "P4ssword" });
      const path = mockLogin.mock.calls[0][0]; // history of first call and first parameter of that call
      expect(path).toBe("/api/1.0/login");
    });
  });
  describe("listUser", () => {
    it("calls /api/1.0/users?page=0&size=3 when no param provided for listUsers", () => {
      const mockListUsers = jest.fn();
      axios.get = mockListUsers;
      apiCalls.listUsers();
      expect(mockListUsers).toBeCalledWith("/api/1.0/users?page=0&size=3");
    });

    it("calls /api/1.0/users?page=5&size=10 when corresponding params are provided for listUsers", () => {
      const mockListUsers = jest.fn();
      axios.get = mockListUsers;
      apiCalls.listUsers({
        page: 5,
        size: 10,
      });
      expect(mockListUsers).toBeCalledWith("/api/1.0/users?page=5&size=10");
    });

    it("calls /api/1.0/users?page=5&size=3 when only page param is provided for listUsers", () => {
      const mockListUsers = jest.fn();
      axios.get = mockListUsers;
      apiCalls.listUsers({
        page: 5,
      });
      expect(mockListUsers).toBeCalledWith("/api/1.0/users?page=5&size=3");
    });

    it("calls /api/1.0/users?page=0&size=5 when only size param is provided for listUsers", () => {
      const mockListUsers = jest.fn();
      axios.get = mockListUsers;
      apiCalls.listUsers({
        size: 5,
      });
      expect(mockListUsers).toBeCalledWith("/api/1.0/users?page=0&size=5");
    });
  });
  describe("getUser", () => {
    it("calls /api/1.0/users/user5 when user5 is provided for getUser", () => {
      const mockGetUser = jest.fn();
      axios.get = mockGetUser;
      apiCalls.getUser("user5");
      expect(mockGetUser).toBeCalledWith("/api/1.0/users/user5");
    });
  });

  describe("updateUser", () => {
    it("calls /api/1.0/users/5 when 5 is provided for updateUser", () => {
      const mockUpdateUser = jest.fn();
      axios.put = mockUpdateUser;
      apiCalls.updateUser("5");
      const path = mockUpdateUser.mock.calls[0][0];
      expect(path).toBe("/api/1.0/users/5");
    });
  });

  describe("postMessage", () => {
    it("calls /api/1.0/messages", () => {
      const mockPostMessage = jest.fn();
      axios.post = mockPostMessage;
      apiCalls.postMessage();
      const path = mockPostMessage.mock.calls[0][0];
      expect(path).toBe("/api/1.0/messages");
    });
  });

  describe("loadMessage", () => {
    it("calls /api/1.0/messages?page=0&size=5&sort=id,desc when no param provided", () => {
      const mockGetMessages = jest.fn();
      axios.get = mockGetMessages;
      apiCalls.loadMessages();
      expect(mockGetMessages).toBeCalledWith(
        "/api/1.0/messages?page=0&size=5&sort=id,desc"
      );
    });

    it("calls /api/1.0/users/user1/messages?page=0&size=5&sort=id,desc when user param is provided", () => {
      const mockGetMessages = jest.fn();
      axios.get = mockGetMessages;
      apiCalls.loadMessages("user1");
      expect(mockGetMessages).toBeCalledWith(
        "/api/1.0/users/user1/messages?page=0&size=5&sort=id,desc"
      );
    });
  });

  describe("loadOldMessages", () => {
    it("calls /api/1.0/messages/5?direction=before&page=0&size=5&sort=id,desc when messageId param provided", () => {
      const mockGetMessages = jest.fn();
      axios.get = mockGetMessages;
      apiCalls.loadOldMessages(5);
      expect(mockGetMessages).toBeCalledWith(
        "/api/1.0/messages/5?direction=before&page=0&size=5&sort=id,desc"
      );
    });

    it("calls /api/1.0/users/user1/messages/5?direction=before&page=0&size=5&sort=id,desc when messageId and username param provided", () => {
      const mockGetMessages = jest.fn();
      axios.get = mockGetMessages;
      apiCalls.loadOldMessages(5, "user1");
      expect(mockGetMessages).toBeCalledWith(
        "/api/1.0/users/user1/messages/5?direction=before&page=0&size=5&sort=id,desc"
      );
    });
  });

  describe("loadNewMessages", () => {
    it("calls /api/1.0/messages/5?direction=after&sort=id,desc when messageId param provided", () => {
      const mockGetMessages = jest.fn();
      axios.get = mockGetMessages;
      apiCalls.loadNewMessages(5);
      expect(mockGetMessages).toBeCalledWith(
        "/api/1.0/messages/5?direction=after&sort=id,desc"
      );
    });

    it("calls /api/1.0/users/user1/messages/5?direction=after&sort=id,desc when messageId and username param provided", () => {
      const mockGetMessages = jest.fn();
      axios.get = mockGetMessages;
      apiCalls.loadNewMessages(5, "user1");
      expect(mockGetMessages).toBeCalledWith(
        "/api/1.0/users/user1/messages/5?direction=after&sort=id,desc"
      );
    });
  });

  describe("loadNewMessagesCount", () => {
    it("calls /api/1.0/messages/5?direction=after&count=true when messageId param provided", () => {
      const mockGetMessages = jest.fn();
      axios.get = mockGetMessages;
      apiCalls.loadNewMessagesCount(5);
      expect(mockGetMessages).toBeCalledWith(
        "/api/1.0/messages/5?direction=after&count=true"
      );
    });

    it("calls /api/1.0/users/user1/messages/5?direction=after&count=true when messageId and username param provided", () => {
      const mockGetMessages = jest.fn();
      axios.get = mockGetMessages;
      apiCalls.loadNewMessagesCount(5, "user1");
      expect(mockGetMessages).toBeCalledWith(
        "/api/1.0/users/user1/messages/5?direction=after&count=true"
      );
    });
  });

  describe("postMessageFile", () => {
    it("calls /api/1.0/messages/upload", () => {
      const mockPostMessageFile = jest.fn();
      axios.post = mockPostMessageFile;
      apiCalls.postMessageFile();
      const path = mockPostMessageFile.mock.calls[0][0];
      expect(path).toBe("/api/1.0/messages/upload");
    });
  });

  describe("deleteMessage", () => {
    it("calls /api/1.0/messages/5 when messageId param provided as 5", () => {
      const mockDelete = jest.fn();
      axios.delete = mockDelete;
      apiCalls.deleteMessage(5);
      const path = mockDelete.mock.calls[0][0];
      expect(path).toBe("/api/1.0/messages/5");
    });
  });

  describe('follow', () => {
    it('calls /api/1.0/users/5/follow when calling the follow with only 5 as user id', () => {
      const mockFollow = jest.fn();
      axios.put = mockFollow;

      apiCalls.follow('5');
      expect(mockFollow).toBeCalledWith('/api/1.0/users/5/follow');
    });

    it('calls /api/1.0/users/5/unfollow when calling the follow with 5 as user id and false for isCallingForFollow', () => {
      const mockUnfollow = jest.fn();
      axios.put = mockUnfollow;

      apiCalls.follow('5', false);
      expect(mockUnfollow).toBeCalledWith('/api/1.0/users/5/unfollow');
    });
  });

  describe('messageReaction', () => {
    it('calls /api/1.0/messages/7/like when calling the messageReaction with 7 and like', () => {
      const mockPut = jest.fn();
      axios.put = mockPut;

      apiCalls.messageReaction(7, 'like');
      expect(mockPut.mock.calls[0][0]).toBe('/api/1.0/messages/7/like');
    });

    it('calls /api/1.0/messages/7/dislike when calling the messageReaction with 7 and dislike', () => {
      const mockPut = jest.fn();
      axios.put = mockPut;

      apiCalls.messageReaction(7, 'dislike');
      expect(mockPut.mock.calls[0][0]).toBe('/api/1.0/messages/7/dislike');
    });

    it('calls /api/1.0/messages/7/like when calling the messageReaction with 7 and random words', () => {
      const mockPut = jest.fn();
      axios.put = mockPut;

      apiCalls.messageReaction(7, 'this is something else');
      expect(mockPut.mock.calls[0][0]).toBe('/api/1.0/messages/7/like');
    });
  })
});

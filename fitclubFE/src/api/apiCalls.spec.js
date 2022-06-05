import axios from "axios";
import * as apiCalls from "./apiCalls";

describe("apiCalls", () => {

  describe("signup", () => {
    it("calls /api/1.0/users", () => {
      const mockSignup = jest.fn();
      axios.post = mockSignup;
      apiCalls.signup();
      const path = mockSignup.mock.calls[0][0];
      expect(path).toBe("/api/1.0/auth/signup");
    });
  });

  describe("login", () => {
    it("calls /api/1.0/login", () => {
      const mockLogin = jest.fn();
      axios.post = mockLogin;
      apiCalls.login({ username: "test-user", password: "P4ssword" });
      const path = mockLogin.mock.calls[0][0]; // history of first call and first parameter of that call
      expect(path).toBe("/api/1.0/auth/login");
    });
  });
  describe("listUser", () => {
    it("calls /api/1.0/users?page=0&size=10 when no param provided for listUsers", () => {
      const mockListUsers = jest.fn();
      axios.get = mockListUsers;
      apiCalls.listUsers();
      expect(mockListUsers).toBeCalledWith("/api/1.0/users?page=0&size=10");
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

    it("calls /api/1.0/users?page=5&size=10 when only page param is provided for listUsers", () => {
      const mockListUsers = jest.fn();
      axios.get = mockListUsers;
      apiCalls.listUsers({
        page: 5,
      });
      expect(mockListUsers).toBeCalledWith("/api/1.0/users?page=5&size=10");
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

  describe("postUserPost", () => {
    it("calls /api/1.0/posts", () => {
      const mockpostUserPost = jest.fn();
      axios.post = mockpostUserPost;
      apiCalls.postUserPost();
      const path = mockpostUserPost.mock.calls[0][0];
      expect(path).toBe("/api/1.0/posts");
    });
  });

  describe("loadPost", () => {
    it("calls /api/1.0/posts?page=0&size=5&sort=id,desc when no param provided", () => {
      const mockGetPosts = jest.fn();
      axios.get = mockGetPosts;
      apiCalls.loadPosts();
      expect(mockGetPosts).toBeCalledWith(
        "/api/1.0/posts?page=0&size=5&sort=id,desc"
      );
    });

    it("calls /api/1.0/users/user1/posts?page=0&size=5&sort=id,desc when user param is provided", () => {
      const mockGetPosts = jest.fn();
      axios.get = mockGetPosts;
      apiCalls.loadPosts("user1");
      expect(mockGetPosts).toBeCalledWith(
        "/api/1.0/users/user1/posts?page=0&size=5&sort=id,desc"
      );
    });
  });

  describe("loadOldPosts", () => {
    it("calls /api/1.0/posts/5?direction=before&page=0&size=5&sort=id,desc when postId param provided", () => {
      const mockGetPosts = jest.fn();
      axios.get = mockGetPosts;
      apiCalls.loadOldPosts(5);
      expect(mockGetPosts).toBeCalledWith(
        "/api/1.0/posts/5?direction=before&page=0&size=5&sort=id,desc"
      );
    });

    it("calls /api/1.0/users/user1/posts/5?direction=before&page=0&size=5&sort=id,desc when postId and username param provided", () => {
      const mockGetPosts = jest.fn();
      axios.get = mockGetPosts;
      apiCalls.loadOldPosts(5, "user1");
      expect(mockGetPosts).toBeCalledWith(
        "/api/1.0/users/user1/posts/5?direction=before&page=0&size=5&sort=id,desc"
      );
    });
  });

  describe("loadNewPosts", () => {
    it("calls /api/1.0/posts/5?direction=after&sort=id,desc when postId param provided", () => {
      const mockGetPosts = jest.fn();
      axios.get = mockGetPosts;
      apiCalls.loadNewPosts(5);
      expect(mockGetPosts).toBeCalledWith(
        "/api/1.0/posts/5?direction=after&sort=id,desc"
      );
    });

    it("calls /api/1.0/users/user1/posts/5?direction=after&sort=id,desc when postId and username param provided", () => {
      const mockGetPosts = jest.fn();
      axios.get = mockGetPosts;
      apiCalls.loadNewPosts(5, "user1");
      expect(mockGetPosts).toBeCalledWith(
        "/api/1.0/users/user1/posts/5?direction=after&sort=id,desc"
      );
    });
  });

  describe("postUserPostFile", () => {
    it("calls /api/1.0/posts/upload", () => {
      const mockpostUserPostFile = jest.fn();
      axios.post = mockpostUserPostFile;
      apiCalls.postUserPostFile();
      const path = mockpostUserPostFile.mock.calls[0][0];
      expect(path).toBe("/api/1.0/posts/upload");
    });
  });

  describe("deletePost", () => {
    it("calls /api/1.0/posts/5 when postId param provided as 5", () => {
      const mockDelete = jest.fn();
      axios.delete = mockDelete;
      apiCalls.deletePost(5);
      const path = mockDelete.mock.calls[0][0];
      expect(path).toBe("/api/1.0/posts/5");
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

  describe('postReaction', () => {
    it('calls /api/1.0/posts/7/like when calling the postReaction with 7 and like', () => {
      const mockPut = jest.fn();
      axios.put = mockPut;

      apiCalls.postReaction(7, 'like');
      expect(mockPut.mock.calls[0][0]).toBe('/api/1.0/posts/7/like');
    });

    it('calls /api/1.0/posts/7/dislike when calling the postReaction with 7 and dislike', () => {
      const mockPut = jest.fn();
      axios.put = mockPut;

      apiCalls.postReaction(7, 'dislike');
      expect(mockPut.mock.calls[0][0]).toBe('/api/1.0/posts/7/dislike');
    });

    it('calls /api/1.0/posts/7/like when calling the postReaction with 7 and random words', () => {
      const mockPut = jest.fn();
      axios.put = mockPut;

      apiCalls.postReaction(7, 'this is something else');
      expect(mockPut.mock.calls[0][0]).toBe('/api/1.0/posts/7/like');
    });
  })
});

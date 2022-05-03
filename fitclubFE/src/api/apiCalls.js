import axios from "axios";

export const signup = (user) => {
  return axios.post("/api/1.0/users", user);
};

export const login = (user) => {
  return axios.post("/api/1.0/login", {}, { auth: user });
};

export const setAuthorizationHeader = ({ username, password, isLoggedIn }) => {
  if (isLoggedIn) {
    axios.defaults.headers.common["Authorization"] = `Basic ${btoa(
      username + ":" + password
    )}`;
  } else {
    delete axios.defaults.headers.common["Authorization"];
  }
};

export const listUsers = (param = { page: 0, size: 3 }) => {
  const path = `/api/1.0/users?page=${param.page || 0}&size=${param.size || 3}`;
  return axios.get(path);
};

export const getUser = (username) => {
  return axios.get(`/api/1.0/users/${username}`);
};

export const updateUser = (userId, body) => {
  return axios.put("/api/1.0/users/" + userId, body);
};

export const postMessage = (message) => {
  return axios.post("/api/1.0/messages", message);
};

export const loadMessages = (username) => {
  const basePath = username
    ? `/api/1.0/users/${username}/messages`
    : "/api/1.0/messages";
  return axios.get(basePath + "?page=0&size=5&sort=id,desc");
};

export const loadOldMessages = (messageId, username) => {
  const basePath = username
    ? `/api/1.0/users/${username}/messages`
    : "/api/1.0/messages";
  const path = `${basePath}/${messageId}?direction=before&page=0&size=5&sort=id,desc`;
  return axios.get(path);
};

export const loadNewMessages = (messageId, username) => {
  const basePath = username
    ? `/api/1.0/users/${username}/messages`
    : "/api/1.0/messages";
  const path = `${basePath}/${messageId}?direction=after&sort=id,desc`;
  return axios.get(path);
};

export const loadNewMessagesCount = (messageId, username) => {
  const basePath = username
    ? `/api/1.0/users/${username}/messages`
    : "/api/1.0/messages";
  const path = `${basePath}/${messageId}?direction=after&count=true`;
  return axios.get(path);
};

export const postMessageFile = (file) => {
  return axios.post("/api/1.0/messages/upload", file);
};

export const deleteMessage = (messageId) => {
  return axios.delete("/api/1.0/messages/" + messageId);
};

export const follow = (userid, isCallingForFollow = true) => {
  return axios.put(`/api/1.0/users/${userid}/${isCallingForFollow ? 'follow' : 'unfollow'}`)
}

export const messageReaction = (id, reaction) => {
  return axios.put(`/api/1.0/messages/${id}/${reaction === 'dislike' ? 'dislike' : 'like'}`);
}
import axios from "axios";

export const signup = (user) => {
  return axios.post("/api/1.0/auth/signup", user);
};

export const login = (user) => {
  return axios.post("/api/1.0/auth/login", user);
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

export const listUsers = (param = { page: 0, size: 10 }) => {
  const path = `/api/1.0/users?page=${param.page || 0}&size=${param.size || 10}`;
  return axios.get(path);
};

export const searchUsers = (searchText, param = { page: 0, size: 10 }) => {
  const path = `/api/1.0/users/find/${searchText}?page=${param.page || 0}&size=${param.size || 10}`;
  return axios.get(path);
};

export const getUser = (username) => {
  return axios.get(`/api/1.0/users/${username}`);
};

export const updateUser = (userId, body, jwt) => {

  const config = {
    headers: { Authorization: `Bearer ${jwt}` }
  };
  return axios.put("/api/1.0/users/" + userId, body, config);
};

export const postMessage = (message, jwt) => {
  const config = {
    headers: { Authorization: `Bearer ${jwt}` }
  };
  return axios.post("/api/1.0/messages", message, config);
};

export const loadMessages = (username, jwt) => {
  const basePath = username
    ? `/api/1.0/users/${username}/messages`
    : "/api/1.0/messages";

  if (jwt != null) {
    const config = {
      headers: { Authorization: `Bearer ${jwt}` }
    };
    return axios.get(basePath + "?page=0&size=5&sort=id,desc", config);
  } else {
    return axios.get(basePath + "?page=0&size=5&sort=id,desc");
  }

};

export const loadOldMessages = (messageId, username, jwt) => {
  const basePath = username
    ? `/api/1.0/users/${username}/messages`
    : "/api/1.0/messages";

  if (jwt != null) {
    const config = {
      headers: { Authorization: `Bearer ${jwt}` }
    };
    const path = `${basePath}/${messageId}?direction=before&page=0&size=5&sort=id,desc`;
    return axios.get(path, config);
  } else {
    const path = `${basePath}/${messageId}?direction=before&page=0&size=5&sort=id,desc`;
    return axios.get(path);
  }

};

export const loadNewMessages = (messageId, username, jwt) => {
  const basePath = username
    ? `/api/1.0/users/${username}/messages`
    : "/api/1.0/messages";

  if (jwt != null) {
    const config = {
      headers: { Authorization: `Bearer ${jwt}` }
    };
    const path = `${basePath}/${messageId}?direction=after&sort=id,desc`;
    return axios.get(path, config);
  } else {
    const path = `${basePath}/${messageId}?direction=after&sort=id,desc`;
    return axios.get(path);
  }
};

export const loadNewMessagesCount = (messageId, username, jwt) => {
  const basePath = username
    ? `/api/1.0/users/${username}/messages`
    : "/api/1.0/messages";

  const config = {
    headers: { Authorization: `Bearer ${jwt}` }
  };

  const path = `${basePath}/${messageId}?direction=after&count=true`;
  return axios.get(path, config);
};

export const postMessageFile = (file, jwt) => {
  const config = {
    headers: { Authorization: `Bearer ${jwt}` }
  };
  return axios.post("/api/1.0/messages/upload", file, config);
};

export const deleteMessage = (messageId, jwt) => {
  const config = {
    headers: { Authorization: `Bearer ${jwt}` }
  };
  return axios.delete("/api/1.0/messages/" + messageId, config);
};

export const follow = (userid, isCallingForFollow = true) => {
  return axios.put(`/api/1.0/users/${userid}/${isCallingForFollow ? 'follow' : 'unfollow'}`)
}

export const messageReaction = (id, reaction, jwt) => {
  const config = {
    headers: { Authorization: `Bearer ${jwt}` }
  };
  return axios.put(`/api/1.0/messages/${id}/${reaction === 'dislike' ? 'dislike' : 'like'}`, config);
}


export const resendEmailVerification = (id, jwt) => {
  const config = {
    headers: { Authorization: `Bearer ${jwt}` }
  };
  return axios.post(`/api/1.0/users/email-verification/confirmation/${id}`, config);
};

// confirm token
export const confirmationToken = (token) => {
  return axios.get(`/api/1.0/users/email-verification/confirmationToken/${token}`);
};


export const changeEmail = (id, jwt) => {
  const config = {
    headers: { Authorization: `Bearer ${jwt}` }
  };
  return axios.post(`/api/1.0/users/email-verification/changeEmail/${id}`, config);
};

// confirm token + body
export const saveChangeEmail = (token, file) => {
  return axios.post(`/api/1.0/users/email-verification/changeEmailToken/` + token, file);
};

export const changePassword = (id, jwt) => {
  const config = {
    headers: { Authorization: `Bearer ${jwt}` }
  };
  return axios.post(`/api/1.0/users/email-verification/changePassword/${id}`, config);
};

// confirm token + body
export const saveNewPassword = (token, file) => {
  return axios.post(`/api/1.0/users/email-verification/passwordReset/` + token, file);
};

export const checkValidToken = (token, identifier) => {
  return axios.get(`/api/1.0/users/isValidToken/${identifier}/${token}`);
};
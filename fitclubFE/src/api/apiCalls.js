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

export const postUserPost = (post, jwt) => {
  const config = {
    headers: { Authorization: `Bearer ${jwt}` }
  };
  return axios.post("/api/1.0/posts", post, config);
};

export const loadPosts = (username, jwt) => {
  const basePath = username
    ? `/api/1.0/users/${username}/posts`
    : "/api/1.0/posts";

  if (jwt != null) {
    const config = {
      headers: { Authorization: `Bearer ${jwt}` }
    };
    return axios.get(basePath + "?page=0&size=5&sort=id,desc", config);
  } else {
    return axios.get(basePath + "?page=0&size=5&sort=id,desc");
  }

};

export const loadOldPosts = (postId, username, jwt) => {
  const basePath = username
    ? `/api/1.0/users/${username}/posts`
    : "/api/1.0/posts";

  if (jwt != null) {
    const config = {
      headers: { Authorization: `Bearer ${jwt}` }
    };
    const path = `${basePath}/${postId}?direction=before&page=0&size=5&sort=id,desc`;
    return axios.get(path, config);
  } else {
    const path = `${basePath}/${postId}?direction=before&page=0&size=5&sort=id,desc`;
    return axios.get(path);
  }

};

export const loadNewPosts = (postId, username, jwt) => {
  const basePath = username
    ? `/api/1.0/users/${username}/posts`
    : "/api/1.0/posts";

  if (jwt != null) {
    const config = {
      headers: { Authorization: `Bearer ${jwt}` }
    };
    const path = `${basePath}/${postId}?direction=after&sort=id,desc`;
    return axios.get(path, config);
  } else {
    const path = `${basePath}/${postId}?direction=after&sort=id,desc`;
    return axios.get(path);
  }
};

export const loadNewPostsCount = (postId, username, jwt) => {
  const basePath = username
    ? `/api/1.0/users/${username}/posts`
    : "/api/1.0/posts";

  const config = {
    headers: { Authorization: `Bearer ${jwt}` }
  };

  const path = `${basePath}/${postId}?direction=after&count=true`;
  return axios.get(path, config);
};

export const postUserPostFile = (file, jwt) => {
  const config = {
    headers: { Authorization: `Bearer ${jwt}` }
  };
  return axios.post("/api/1.0/posts/upload", file, config);
};

export const deletePost = (postId, jwt) => {
  const config = {
    headers: { Authorization: `Bearer ${jwt}` }
  };
  return axios.delete("/api/1.0/posts/" + postId, config);
};

export const follow = (userId, isCallingForFollow = true, jwt) => {
  const config = {
    headers: { Authorization: `Bearer ${jwt}` }
  };
  return axios.put(`/api/1.0/users/${userId}/${isCallingForFollow ? 'follow' : 'unfollow'}`, config)
}

export const postReaction = (id, reaction, jwt) => {
  const config = {
    headers: { Authorization: `Bearer ${jwt}` }
  };
  return axios.put(`/api/1.0/posts/${id}/${reaction === 'dislike' ? 'dislike' : 'like'}`, config);
}


export const resendEmailVerification = (id, jwt) => {
  const config = {
    headers: { Authorization: `Bearer ${jwt}` }
  };
  return axios.post(`/api/1.0/users/email-verification/confirmation/${id}`, config);
};

export const checkEmailVerification = (id, jwt) => {
  const config = {
    headers: { Authorization: `Bearer ${jwt}` }
  };
  return axios.post(`/api/1.0/users/email-verification-check/confirmation/${id}`, config);
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

export const recoverPasswordByEmail = (email) => {
  return axios.post("/api/1.0/users/recoverPassword", email);
};
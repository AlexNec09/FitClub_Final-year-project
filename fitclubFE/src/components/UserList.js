import React, { useState, useEffect, useCallback } from "react";
import * as apiCalls from "../api/apiCalls";
import UserListItem from "./UserListItem";

const UserList = (props) => {
  const [page, setPage] = useState({
    content: [],
    number: 0,
    size: 3,
  });

  const [loadError, setLoadError] = useState();

  const loadData = useCallback(
    (requestedPage = 0) => {
      apiCalls
        .listUsers({ page: requestedPage, size: page.size })
        .then((response) => {
          setPage(response.data);
          setLoadError();
        })
        .catch((error) => {
          setLoadError("User load failed");
        });
    },
    [page.size]
  );

  useEffect(() => {
    loadData();
  }, [loadData]);

  const onClickNext = () => {
    loadData(page.number + 1);
  };

  const onClickPrevious = () => {
    loadData(page.number - 1);
  };

  const { content, first, last } = page;

  return (
    <div className="card">
      <h3 className="card-title m-auto">Users</h3>
      <div className="list-group list-group-flush" data-testid="usergroup">
        {content.map((user) => {
          return <UserListItem key={user.username} user={user} />;
        })}
      </div>
      <div className="clearfix">
        <div>
          {!first && (
            <span
              className="badge rounded-pill bg-light text-dark float-start"
              style={{ cursor: "pointer" }}
              onClick={onClickPrevious}
            >
              {`< previous`}
            </span>
          )}

          {!last && (
            <span
              className="badge rounded-pill bg-light text-dark float-end"
              style={{ cursor: "pointer" }}
              onClick={onClickNext}
            >
              next {">"}
            </span>
          )}
        </div>
      </div>
      {loadError && (
        <span className="text-center text-danger">{loadError}</span>
      )}
    </div>
  );
};

console.error = () => {};
export default UserList;

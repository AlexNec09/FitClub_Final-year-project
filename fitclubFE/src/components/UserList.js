import React, { useState, useEffect, useCallback } from "react";
import * as apiCalls from "../api/apiCalls";
import UserListItem from "./UserListItem";
import { connect } from "react-redux";


const UserList = (props) => {
  const [search, setSearch] = useState();
  const [infoText, setInfoText] = useState("No users found!");

  const [page, setPage] = useState({
    content: [],
    number: 0,
    size: 10
  });


  const [loadError, setLoadError] = useState();

  const loadData = useCallback(
    (requestedPage = 0) => {
      if (search == null || search === "") {
        apiCalls
          .listUsers({ page: requestedPage, size: page.size })
          .then((response) => {
            setPage(response.data);
            if (response.data.content.length !== 0) {
              setInfoText("Users")
            }
            setLoadError();
          })
          .catch((error) => {
            setLoadError("Unable to load users!");
          });
      } else {
        apiCalls
          .searchUsers(search, { page: requestedPage, size: page.size })
          .then((response) => {
            setPage(response.data);
            if (response.data.content.length !== 0) {
              setInfoText("Users")
            }
            setLoadError();
          })
          .catch((error) => {
            setLoadError("User search failed!");
          });
      }

    },
    [page.size, search]
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
    <div className="container pt-4 mt-2 pb-5 mb-5">
      {
        props.loggedInUser.isLoggedIn && (<div className="input-group">
          <input className="form-control border-end-1 border rounded-pill mb-3" placeholder="Search"
            type="search"
            value={search}
            onChange={(event) => setSearch(event.target.value)}
          />
        </div>)}
      <div className="card">
        <h3 className="card-title m-auto textUserList">{infoText}</h3>
        <div className="list-group list-group-flush" data-testid="usergroup">
          {content.map((user) => {
            return <UserListItem key={user.username} user={user} />;
          })}
        </div>
        <div className="clearfix">
          <div>
            {(!first && (page.content.length > 0 || loadError === undefined)) && (
              <span
                className="badge rounded-pill bg-light text-dark float-start"
                style={{ cursor: "pointer" }}
                onClick={onClickPrevious}
              >
                {`< previous`}
              </span>
            )}

            {(!last && (page.content.length > 0 || loadError === undefined)) && (
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
          <span className="text-center text-danger pt-3">{loadError}</span>
        )}
      </div>
    </div>
  );
};

console.error = () => { };

const mapStateToProps = (state) => {
  return {
    loggedInUser: state,
  };
};

export default connect(mapStateToProps)(UserList);

import React from "react";
import UserList from "../components/UserList";
import { connect } from "react-redux";
import PostFeed from "../components/PostFeed";
import { Link } from 'react-router-dom';
import FeedPage from '../assets/FeedPage.png';
import Users from '../assets/Users.png';
import MyProfile from '../assets/myProfile.png';

import Tab from 'react-bootstrap/Tab';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import Nav from 'react-bootstrap/Nav';


class HomePage extends React.Component {
  render() {
    return (
      <div className="background-image pt-5" id="background-image">

        <div className="container pt-2">

          <div data-testid="homepage">

            <Tab.Container id="left-tabs-example" defaultActiveKey="first">
              <Row className="pt-2 ">

                <Col className="" sm={3}>

                  <Nav variant="pills" className="flex-column sticky-menu">
                    <Nav.Item>
                      <Nav.Link eventKey="first" className="my-nav-item">
                        {/* <i className="fas fa-home text-secondary pr-2"></i> */}
                        <img src={FeedPage} width="40" alt="FeedPage" />
                        <span className="pl-2"> Feeds</span>
                      </Nav.Link>
                    </Nav.Item>
                    <Nav.Item>
                      <Nav.Link eventKey="second" className="my-nav-item">
                        {/* <i className="fas fa-heart text-secondary pr-2"></i> */}
                        <img src={Users} width="40" alt="Users" />
                        <span className="pl-2"> Users</span>
                      </Nav.Link>
                    </Nav.Item>

                    {this.props.loggedInUser.isLoggedIn && (<div>
                      <hr width="98%"></hr>

                      <Link to={{
                        pathname: `/users/${this.props.loggedInUser.username}`,
                        tabValue: '1',
                      }}
                        className="my-nav-item"
                        style={{ textDecoration: 'none' }}
                      >
                        <img src={MyProfile} width="39" alt="MyProfile" />
                        <span className="pl-2"> My Profile</span>
                      </Link>
                    </div>)}
                  </Nav>

                </Col>

                <Col sm={9}>
                  <Tab.Content>

                    {/* Feed Page */}
                    <Tab.Pane eventKey="first">
                      <PostFeed />
                    </Tab.Pane>

                    {/* Users */}
                    <Tab.Pane eventKey="second">
                      <UserList />
                    </Tab.Pane>

                  </Tab.Content>

                </Col>
              </Row>
            </Tab.Container>
          </div>
        </div>
      </div>
    );
  }
}

const mapStateToProps = (state) => {
  return {
    loggedInUser: state,
  };
};

export default connect(mapStateToProps)(HomePage);
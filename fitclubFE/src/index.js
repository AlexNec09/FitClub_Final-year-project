import React from "react";
import ReactDOM from "react-dom";
import "./index.css";
import { HashRouter } from "react-router-dom";
import "../src/main.scss";
import App from "./containers/App";
import { Provider } from "react-redux";
import configureStore from "./redux/configureStore";
import 'bootstrap/dist/css/bootstrap.min.css';


const store = configureStore();

ReactDOM.render(
  <Provider store={store}>
    <HashRouter>
      <App />
    </HashRouter>
  </Provider>,
  document.getElementById("root")
);

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
// reportWebVitals();

// serviceWorker.unregister();

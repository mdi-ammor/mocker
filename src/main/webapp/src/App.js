import React, { useState } from 'react';
import axios from "axios";
import LeftSide from './components/left-side/LeftSide';
import RigthSide from './components/rigth-side/RigthSide';

import './App.css';


function App() {
  const [data, setData] = useState(null);
  const [selectEndpointKey, setSelectEndpointKey] = useState(null);

  const loadEndpoint = (endpointKey) => {
    axios.get( `http://localhost:9000/ihm/endpoints/${endpointKey}`).then((response) => {
      setSelectEndpointKey(endpointKey);
      setData(response.data);
    });
  }

  return (
    <div className="App">
      <header className="App-header">
        Mock Generator
      </header>
      <LeftSide selectEndpointKey={selectEndpointKey} refreshRightSide={loadEndpoint} />
      <RigthSide data={data} />
    </div>
  );
}

export default App;

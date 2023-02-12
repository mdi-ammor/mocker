import axios from "axios";
import React, { useState, useEffect } from "react";
import { DiDoctrine } from "react-icons/di";
import { TbPennant } from "react-icons/tb";
import './LeftSide.css'

const plus = '+';

function LeftSide(props) {

    const [endpoints, setEndpoints] = useState(null);

    useEffect(() => {
        axios.get("http://localhost:9000/ihm/endpoints").then((response) => {
            setEndpoints(response.data);
        });
    }, [props]);

    if(endpoints)
        return (
            <div className="leftSide">
                <div className="leftSide-title">
                    Configured endpoints
                    <hr />
                </div>
                <div className="leftSide-endpoints">
                <div>
                    {endpoints.map(endpoint => {
                        const selected = (endpoint.replace(/\//g, '') === props.selectEndpointKey) ? <TbPennant className="leftSide-flag"/> : '';
                        return (<button key={endpoint} className="leftSide-button" onClick={() => props.refreshRightSide(endpoint.replace(/\//g, ''))}>
                        <DiDoctrine /> {endpoint.replace(/\/mock/, '')}{selected}</button>)

                    })}
                </div>
                </div>
                <button className="leftSide-add">{plus}</button>
            </div>
        );
    return null;
}
export default LeftSide;
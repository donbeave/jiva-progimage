import http from 'k6/http';
import {check} from 'k6';

export let options = {
  vus: 30,
  duration: "10s"
};

export default function () {
  let res = http.get('http://localhost:10001/health');

  check(res, {
    'is status 200': (r) => r.status === 200
  })
}

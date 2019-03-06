import {commonEnvironment} from './environment.common';

export const environment = {
  production: true,
  firebaseConfig: commonEnvironment.firebaseConfig,
  host: commonEnvironment.host,
  actor: commonEnvironment.actor
};

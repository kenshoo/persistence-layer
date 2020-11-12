package com.kenshoo.pl.entity;

/**
 * Created by dimag on 05/11/2015.
 */
public enum SupportedChangeOperation {

    READ_ONLY {
        @Override
        public boolean supports(ChangeOperation operation) {
            return false;
        }
    },

    CREATE {
        @Override
        public boolean supports(ChangeOperation operation) {
            return operation == ChangeOperation.CREATE;
        }
    },
    UPDATE {
        @Override
        public boolean supports(ChangeOperation operation) {
            return operation == ChangeOperation.UPDATE;
        }
    },
    DELETE {
        @Override
        public boolean supports(ChangeOperation operation) {
            return operation == ChangeOperation.DELETE;
        }
    },
    CREATE_AND_UPDATE {
        @Override
        public boolean supports(ChangeOperation operation) {
            return operation == ChangeOperation.CREATE || operation == ChangeOperation.UPDATE;
        }
    },
    CREATE_UPDATE_AND_DELETE {
        @Override
        public boolean supports(ChangeOperation operation) {
            return operation == ChangeOperation.CREATE || operation == ChangeOperation.UPDATE || operation == ChangeOperation.DELETE;
        }
    },

    UPDATE_AND_DELETE {
        @Override
        public boolean supports(ChangeOperation operation) {
            return operation == ChangeOperation.UPDATE || operation == ChangeOperation.DELETE;
        }
    };

    public abstract boolean supports(ChangeOperation operation);

}

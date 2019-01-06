package de.hshannover.inform.gnuman.app.mapeditor;

import java.util.Stack;

/**
 * Manage Undo/Redo events.
 * @author Marc Herschel
 */

class OperationStack {
    private Stack<Operation> stack;
    private StackChanged notifier;

    /**
     * Construct a new OperationStack
     * @param notifier what to do if our stack size changes.
     */
    OperationStack(StackChanged notifier) {
        stack = new Stack<>();
        this.notifier = notifier;
    }

    void clear() {
        stack.clear();
        notifier.sizeChanged();
    }

    void push(Operation o) {
        stack.push(o);
        notifier.sizeChanged();
    }

    Operation pop() {
        Operation o = stack.pop();
        notifier.sizeChanged();
        return o;
    }

    int size() {
        return stack.size();
    }
}

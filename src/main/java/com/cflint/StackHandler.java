package com.cflint;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StackHandler {

    final Deque<Stack> varStack = new ArrayDeque<Stack>();
    final Set<String> excludes = new HashSet<String>();

    public StackHandler() {
        super();
        excludes.add("VARIABLES");
        excludes.add("ARGUMENTS");
        excludes.add("SUPER");
        excludes.add("SESSION");
        excludes.add("APPLICATION");
        excludes.add("THIS");
        excludes.add("LOCAL");
        varStack.push(new Stack(""));
    }

    public void addVariable(final String name) {
        varStack.peek().getVariables().add(name.toUpperCase());
    }
    public void addVariables(final List<String> names) {
    	if(names != null){
	    	for(String name: names){
	    		varStack.peek().getVariables().add(name.toUpperCase());
	    	}
    	}
    }
    public void addQueryColumnSet(final String name, final List<String> columns) {
        varStack.peek().getQueryColumns().put(name.toUpperCase(),columns);
    }

    public void addArgument(final String name) {
        varStack.peek().getArguments().add(name.toUpperCase());
    }

    public boolean hasArgument(final String name) {
        return varStack.peek().getArguments().contains(name.toUpperCase());
    }

    public void push(final String pathName) {
        varStack.push(new Stack(pathName));
    }

    public Stack pop() {
        return varStack.pop();
    }

    public Object getPluginVar(final Class<?> clazz, final String var) {
        final String key = clazz.getName() + "_" + var;
        final Iterator<Stack> iter = varStack.iterator();
        while (iter.hasNext()) {
            final Stack vars = iter.next();
            if (vars.getPluginvariables().containsKey(key)) {
                return vars.getPluginvariables().get(key);
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public Collection<Object> getPluginAllVars(final Class<?> clazz, final String var) {
        final List<Object> retval = new ArrayList<Object>();
        final String key = clazz.getName() + "_" + var;
        final Iterator<Stack> iter = varStack.iterator();
        while (iter.hasNext()) {
            final Stack vars = iter.next();
            if (vars.getPluginvariables().containsKey(key)) {
                final Object value = vars.getPluginvariables().get(key);
                if (value instanceof Collection<?>) {
                    retval.addAll((Collection<? extends Object>) value);
                } else {
                    retval.add(value);
                }
            }
        }
        return retval;
    }

    public void setPluginVar(final Class<?> clazz, final String var, final Object value) {
        final String key = clazz.getName() + "_" + var;
        varStack.peek().getPluginvariables().put(key, value);
    }

    public boolean isVariable(final String name) {
        final Iterator<Stack> iter = varStack.iterator();
        while (iter.hasNext()) {
            final Stack vars = iter.next();
            if (vars.getVariables().contains(name.toUpperCase())) {
                return true;
            }
        }
        return false;
    }
    
    public List<String> getQueryColumns(final String name) {
        final Iterator<Stack> iter = varStack.iterator();
        while (iter.hasNext()) {
            final Stack vars = iter.next();
            if (vars.getQueryColumns().containsKey(name.toUpperCase())) {
                return vars.getQueryColumns().get(name.toUpperCase());
            }
        }
        return null;
    }

    public boolean isArgument(final String name) {
        final Iterator<Stack> iter = varStack.iterator();
        while (iter.hasNext()) {
            final Stack vars = iter.next();
            if (vars.getArguments().contains(name.toUpperCase())) {
                return true;
            }
        }
        return false;
    }

    public boolean checkVariable(final String name) {
    	if (excludes.contains(name.toUpperCase())) {
            return true;
        }
        final Iterator<Stack> iter = varStack.iterator();
        while (iter.hasNext()) {
            final Stack vars = iter.next();
            // Report only once per scope level
            if (vars.getReported().contains(name.toUpperCase())) {
                return true;
            }
            if (vars.getVariables().contains(name.toUpperCase())) {
                return true;
            }
            if (vars.getArguments().contains(name.toUpperCase())) {
                // Dereference of an argument without the arguments scope copies
                // to local scope
                vars.getVariables().add(name.toUpperCase());
                return true;
            }
        }
        // reported once gets pushed to avoid duplicates
        varStack.peek().getReported().add(name.toUpperCase());
        return false;
    }

    public static class Stack {
        private Set<String> variables = new HashSet<String>();
        private Map<String,List<String>> queryColumns = new HashMap<String,List<String>>();
        private Set<String> reported = new HashSet<String>();
        private Map<String, Object> pluginvariables = new HashMap<String, Object>();
        private Set<String> arguments = new HashSet<String>();
        private String pathName;

        public Stack(final String pathName) {
            super();
            this.pathName = pathName;
        }

        public String getPathName() {
            return pathName;
        }

        public void setPathName(final String pathName) {
            this.pathName = pathName;
        }

        public Set<String> getVariables() {
            return variables;
        }

        public Map<String, List<String>> getQueryColumns() {
			return queryColumns;
		}

		public Set<String> getArguments() {
            return arguments;
        }

        public Map<String, Object> getPluginvariables() {
            return pluginvariables;
        }

        public Set<String> getReported() {
            return reported;
        }

    }
}

import random
from environment import Agent, Environment
from planner import RoutePlanner
from simulator import Simulator
import numpy as np

class LearningAgent(Agent):
    """An agent that learns to drive in the smartcab world."""

    def __init__(self, env):
        super(LearningAgent, self).__init__(env)  # sets self.env = env, state = None, next_waypoint = None, and a default color
        self.color = 'red'  # override color
        self.planner = RoutePlanner(self.env, self)  # simple route planner to get next_waypoint
        # TODO: Initialize any additional variables here
        self.Q = np.zeros((9,4))
        self.Q[1][2] = 1
        self.Q[3][3] = 1
        self.Q[6][3] = 1
        self.Q[7][1] = 1
        self.Q[0][2] = -1
        self.Q[2][2] = -1
        self.Q[4][3] = -1
        self.Q[5][3] = -1
        self.Q[8][1] = -1
        self.gamma = 0.8
        self.alpha = 0.3
        self.epsilon = 0.6
        self.epsilon_start = 0.6
        self.n_updates = 0
        # possible states, these 9 states represents all the 96 possible states given 
        # the values of the variables used to define a state
        self.states = np.array([
            ['left', 'green', 'forward', None],
            ['left', 'green', None, None],
            ['left', 'red', None, None],
            ['right', 'green', None, None],
            ['right', 'red', None, 'forward'],
            ['right', 'red', 'left', None],
            ['right', 'red', None, None],
            ['forward', 'green', None, None],
            ['forward', 'red', None, None]
        ])

    def reset(self, destination=None):
        self.planner.route_to(destination)
        # TODO: Prepare for a new trip; reset any variables here, if required

    # Reduces the state to one of those in self.states.
    def reduce_state(self, state):
        if state[2] == 'None':
            np.put(state, [2], [None])
        if state[3] == 'None':
            np.put(state, [3], [None])
        if state[0] == 'left':
            if state[1] == 'green':
                if state[2] == 'forward':
                    np.put(state, [3], [None])
                else:
                    np.put(state, [2], [None])
                    np.put(state, [3], [None])
            else: # red
                np.put(state, [2], [None])
                np.put(state, [3], [None])
        elif state[0] == 'right':
            if state[1] == 'green':
                np.put(state, [2], [None])
                np.put(state, [3], [None])
            else: # red
                if state[3] == 'forward':
                    np.put(state, [2], [None])
                elif state[2] == 'left':
                    np.put(state, [3], [None])
                else:
                    np.put(state, [2], [None])
                    np.put(state, [3], [None])
        else: # 'forward'
            np.put(state, [2], [None])
            np.put(state, [3], [None])

    # Position of the given state in the array self.states
    def get_index_reduced_state(self, state):
        i = -1
        for x in range(self.states.shape[0]):
            s = np.array(self.states[x,:])
            if (state == s).all():
                i = x
                break

        return i

    def next_action(self, state):
        index = self.get_index_reduced_state(state)
        if index >= 0:
            next_actions = self.Q[index]
            max_reward = np.amax(next_actions)
            preferred_actions_indices = np.where(next_actions == max_reward)[0]
            return Environment.valid_actions[np.random.choice(preferred_actions_indices)]

    def update_Q(self, state, action, reward):
        index_state = self.get_index_reduced_state(state)
        index_action = Environment.valid_actions.index(action)
        q_s_a = self.Q[index_state][index_action]

        # sense env
        next_waypoint = self.planner.next_waypoint()
        inputs = self.env.sense(self)
        next_state = np.array([next_waypoint, inputs['light'], inputs['oncoming'], inputs['left']])
        self.reduce_state(next_state)

        # argmax R(s', a')
        next_action = self.next_action(next_state)
        index_next_state = self.get_index_reduced_state(next_state)
        index_next_action = Environment.valid_actions.index(next_action)
        q_ss_aa = self.Q[index_next_state][index_next_action]

        n_q_s_a = q_s_a + (self.alpha * (reward + self.gamma * q_ss_aa - q_s_a ))

        self.Q[index_state][index_action] = n_q_s_a

    def update(self, t):
        # Gather inputs
        self.next_waypoint = self.planner.next_waypoint()  # from route planner, also displayed by simulator
        inputs = self.env.sense(self)
        deadline = self.env.get_deadline(self)

        # TODO: Update state
        self.state = np.array([self.next_waypoint, inputs['light'], inputs['oncoming'], inputs['left']])
        self.reduce_state(self.state)

        output = "\n LearningAgent.update(): state = {}".format(self.state)
        print output

        # TODO: Select action according to your policy
        self.n_updates = self.n_updates + 1;
        #action = random.choice(self.env.valid_actions)
        if self.n_updates % 70 == 0:
            self.epsilon = 0.9 * self.epsilon

        if random.random() < self.epsilon:
            action = random.choice(Environment.valid_actions)
        else:
            action = self.next_action(self.state)

        # Execute action and get reward
        reward = self.env.act(self, action)

        # TODO: Learn policy based on state, action, reward
        self.update_Q(self.state, action, reward)

        output = "\n LearningAgent.update(): deadline = {}, inputs = {}, action = {}, reward = {}".format(deadline, inputs, action, reward)  # [debug]
        print output

def run():
    """Run the agent for a finite number of trials."""
    # Set up environment and agent
    e = Environment()  # create environment (also adds some dummy traffic)
    a = e.create_agent(LearningAgent)  # create agent
    e.set_primary_agent(a, enforce_deadline=False)  # specify agent to track
    # NOTE: You can set enforce_deadline=False while debugging to allow longer trials

    # Now simulate it
    sim = Simulator(e, update_delay=0, display=False)  # create simulator (uses pygame when display=True, if available)
    # NOTE: To speed up simulation, reduce update_delay and/or set display=False

    sim.run(n_trials=100)  # run for a specified number of trials
    # NOTE: To quit midway, press Esc or close pygame window, or hit Ctrl+C on the command-line

if __name__ == '__main__':
    run()
